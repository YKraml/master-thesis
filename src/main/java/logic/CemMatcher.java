package logic;

import io.json.CemProjectReaderWriter;
import jakarta.inject.Inject;
import model.writable.CemProject;
import model.writable.CemProjectPair;
import model.writable.Proof;
import util.CemCommitsUtil;
import util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CemMatcher {

    private final CemProjectReaderWriter cemProjectReaderWriter;
    private final CemCommitsUtil cemCommitsUtil;

    @Inject
    public CemMatcher(CemProjectReaderWriter cemProjectReaderWriter, CemCommitsUtil cemCommitsUtil) {
        this.cemProjectReaderWriter = cemProjectReaderWriter;
        this.cemCommitsUtil = cemCommitsUtil;
    }

    public List<CemProjectPair> match(List<Proof> allTreatedProofs, List<Proof> allControlProofs) {
        List<CemProjectPair> result = new ArrayList<>();

        // Group all treated projects
        System.out.println("Group all treated projects");
        Map<String, List<CemProject>> treatedStrata = createStrata(allTreatedProofs);

        // Group all control projects
        System.out.println("Group all control projects");
        Map<String, List<CemProject>> controlStrata = createStrata(allControlProofs);

        // Iterate over strata that exist in the treated set
        int k = 1;
        for (String key : treatedStrata.keySet()) {
            System.out.println("Working on " + k + "/" + treatedStrata.size() + " key: " + key);
            k++;

            List<CemProject> treatedBin = treatedStrata.get(key);
            List<CemProject> controlsBin = controlStrata.get(key);

            // If no controls exist for this stratum, we cannot make any pairs here. Skip.
            if (controlsBin == null || controlsBin.isEmpty()) {
                continue;
            }

            // Shuffle both lists to ensure random pairing within the stratum
            Collections.shuffle(treatedBin, Constants.RANDOM);
            Collections.shuffle(controlsBin, Constants.RANDOM);

            // Maximize matches: The number of pairs is the size of the smaller list
            int numberOfMatches = Math.min(treatedBin.size(), controlsBin.size());

            // Create the twins
            for (int i = 0; i < numberOfMatches; i++) {
                CemProject treated = treatedBin.get(i);
                CemProject control = controlsBin.get(i);
                CemProjectPair pair = createPair(treated, control);
                result.add(pair);
            }
        }
        System.out.println("Finished loop");

        return result;
    }

    private CemProjectPair createPair(CemProject treated, CemProject control) {
        CemProject newTreated = new CemProject(
                treated.repoOwner(),
                treated.repoName(),
                treated.groupType(),
                treated.numberOfStars(),
                treated.creationYear(),
                treated.defaultBranch(),
                treated.allCommits().size(),
                treated.loc(),
                treated.sloc(),
                treated.numberOfContributors(),
                treated.linterCommits(),
                treated.allCommits()
        );

        CemProject newControl = new CemProject(
                control.repoOwner(),
                control.repoName(),
                control.groupType(),
                control.numberOfStars(),
                control.creationYear(),
                control.defaultBranch(),
                control.allCommits().size(),
                control.loc(),
                control.sloc(),
                control.numberOfContributors(),
                getControlLinterCommits(control.allCommits(), treated.allCommits(), treated.linterCommits()), // Insert newly calculated faked linter commit list
                control.allCommits()
        );

        return new CemProjectPair(newTreated, newControl);
    }

    private Map<String, List<CemProject>> createStrata(List<Proof> proofs) {
        return proofs.stream()
                .map(proof -> {
                    try {
                        return cemProjectReaderWriter.read(proof.repoOwner(), proof.repoName()).orElseThrow();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.groupingBy(this::generateStratumKey));
    }

    /**
     * Create fake linter commits for control project.
     */
    private List<CemProject.LinterCommit> getControlLinterCommits(List<CemProject.Commit> controlCommits, List<CemProject.Commit> treatedCommits, List<CemProject.LinterCommit> treatedLinterCommits) {
        List<CemProject.LinterCommit> controlLinterCommits = new ArrayList<>();

        for (CemProject.LinterCommit linterCommit : treatedLinterCommits) {
            int index = (int) (((double) linterCommit.index()) / treatedCommits.size() * controlCommits.size());
            CemProject.Commit fakedLinterCommit = controlCommits.get(index);
            String name = fakedLinterCommit.name();
            int time = fakedLinterCommit.time();
            String linterName = "CONTROL";
            int numberOfCommitsInTwoYearWindow = cemCommitsUtil.calcNumberOfCommitsInAnalysisWindow(controlCommits, fakedLinterCommit);
            String user = fakedLinterCommit.user();
            controlLinterCommits.add(new CemProject.LinterCommit(name, time, index, linterName, user, numberOfCommitsInTwoYearWindow));
        }
        return controlLinterCommits;
    }

    private String generateStratumKey(CemProject project) {
        String starBin = createLogBin(project.numberOfStars(), 2);
        String commitsBin = createLogBin(project.numberOfCommits(), 2);
        int yearBin = Integer.parseInt(project.creationYear());
        String slocBin = createLogBin(project.sloc(), 2);
        String contributorsBin = createLogBin(project.numberOfContributors(), 2);

        // combine to key
        return "STARS[%s]_YEAR[%d]_COMMITS[%s]_SLOC[%s]_CONTRIBUTORS[%s]".formatted(starBin, yearBin, commitsBin, slocBin, contributorsBin);
    }

    private static String createLogBin(int commits, double base) {
        //Log scale: n bins between 10 and 100, 100 and 1000, 1000 and 10.000, ...
        double logValue = Math.log10(commits);

        double lowerBoundExp = Math.floor(logValue * base) / base;
        double upperBoundExp = lowerBoundExp + 1 / base;

        long lowerBound = Math.round(Math.pow(10, lowerBoundExp));
        long upperBound = Math.round(Math.pow(10, upperBoundExp));

        return lowerBound + "-" + upperBound;
    }
}