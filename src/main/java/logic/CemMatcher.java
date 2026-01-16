package logic;

import io.json.CemProjectPairReaderWriter;
import io.json.CemProjectReaderWriter;
import jakarta.inject.Inject;
import model.CemProject;
import model.CemProjectPair;
import model.Proof;
import util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CemMatcher {

    private static final double NUMBER_OF_BUCKETS_PER_POWER_OF_TEN = 10;

    private final CemProjectPairReaderWriter projectPairReaderWriter;
    private final CemProjectReaderWriter cemProjectReaderWriter;

    @Inject
    public CemMatcher(CemProjectPairReaderWriter projectPairReaderWriter, CemProjectReaderWriter cemProjectReaderWriter) {
        this.projectPairReaderWriter = projectPairReaderWriter;
        this.cemProjectReaderWriter = cemProjectReaderWriter;
    }

    public void matchAndSafe(List<Proof> allTreatedProofs, List<Proof> allControlProofs) throws IOException {

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

                try {
                    projectPairReaderWriter.write(createPair(treated, control));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("Finished loop");
    }

    private static CemProjectPair createPair(CemProject treated, CemProject control) {
        CemProject newTreated = new CemProject(
                treated.repoOwner(),
                treated.repoName(),
                treated.groupType(),
                treated.numberOfStars(),
                treated.creationYear(),
                treated.defaultBranch(),
                treated.allCommits().size(),
                treated.linterCommits(),
                null
        );

        CemProject newControl = new CemProject(
                control.repoOwner(),
                control.repoName(),
                control.groupType(),
                control.numberOfStars(),
                control.creationYear(),
                control.defaultBranch(),
                control.allCommits().size(),
                getControlLinterCommits(control.allCommits(), treated.allCommits(), treated.linterCommits()), // Insert newly calculated faked linter commit list
                null
        );

        return new CemProjectPair(newTreated, newControl);
    }

    private Map<String, List<CemProject>> createStrata(List<Proof> allTreatedProofs) {
        return allTreatedProofs.stream()
                .map(proof -> {
                    try {
                        return cemProjectReaderWriter.getElement(proof.repoOwner(), proof.repoName()).get();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.groupingBy(this::generateStratumKey));
    }

    private static List<CemProject.Commit> getControlLinterCommits(List<CemProject.Commit> controlCommits, List<CemProject.Commit> treatedCommits, List<CemProject.Commit> treatedLinterCommits) {
        List<CemProject.Commit> controlLinterCommits = new ArrayList<>();

        for (CemProject.Commit linterCommit : treatedLinterCommits) {
            int index = linterCommit.index() * controlCommits.size() / treatedCommits.size();
            String name = controlCommits.get(index).name();
            int time = controlCommits.get(index).time();
            controlLinterCommits.add(new CemProject.Commit(name, time, index));
        }
        return controlLinterCommits;
    }

    private String generateStratumKey(CemProject project) {

        // Group by number of stars
        String starBin = createLogBin(project.numberOfStars());

        // Group by number of commits
        String commitsBin = createLogBin(project.numberOfCommits());

        // Group by year
        int year = Integer.parseInt(project.creationYear());

        // combine to key
        return "STARS[%s]_YEAR[%d]_COMMITS[%s]".formatted(starBin, year, commitsBin);
    }

    private static String createLogBin(int commits) {
        //Log scale: n bins between 10 and 100, 100 and 1000, 1000 and 10.000, ...
        double logValue = Math.log10(commits);

        double lowerBoundExp = Math.floor(logValue * NUMBER_OF_BUCKETS_PER_POWER_OF_TEN) / NUMBER_OF_BUCKETS_PER_POWER_OF_TEN;
        double upperBoundExp = lowerBoundExp + 1 / NUMBER_OF_BUCKETS_PER_POWER_OF_TEN;

        long lowerBound = Math.round(Math.pow(10, lowerBoundExp));
        long upperBound = Math.round(Math.pow(10, upperBoundExp));

        return lowerBound + "-" + upperBound;
    }
}