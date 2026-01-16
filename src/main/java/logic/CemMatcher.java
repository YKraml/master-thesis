package logic;

import io.git.BareRepositoryDownloader;
import io.json.CemProjectPairReaderWriter;
import jakarta.inject.Inject;
import model.CemProject;
import model.CemProjectPair;
import model.Proof;
import util.Constants;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CemMatcher {

    private static final double NUMBER_OF_BUCKETS_PER_POWER_OF_TEN = 4;

    private final BareRepositoryDownloader bareRepositoryDownloader;
    private final CemProjectPairReaderWriter projectPairReaderWriter;

    @Inject
    public CemMatcher(BareRepositoryDownloader bareRepositoryDownloader, CemProjectPairReaderWriter projectPairReaderWriter) {
        this.bareRepositoryDownloader = bareRepositoryDownloader;
        this.projectPairReaderWriter = projectPairReaderWriter;
    }

    public void matchAndSafe(Map<CemProject, Proof> allTreatedProjects, Map<CemProject, Proof> allUntreatedProjects) {

        // Group all treated projects
        System.out.println("Group all treated projects");
        Map<String, List<CemProject>> treatedStrata = allTreatedProjects.keySet().stream()
                .collect(Collectors.groupingBy(this::generateStratumKey));

        // Group all control projects
        System.out.println("Group all control projects");
        Map<String, List<CemProject>> controlStrata = allUntreatedProjects.keySet().stream()
                .collect(Collectors.groupingBy(this::generateStratumKey));

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
            IntStream.range(0, numberOfMatches).parallel().forEach(i -> {
                CemProject treated = treatedBin.get(i);
                CemProject control = controlsBin.get(i);

                //Check if pair already exists
                if (pairExists(treated)) {
                    return;
                }

                List<CemProject.Commit> treatedCommits = bareRepositoryDownloader.getCommits(treated.repoOwner(), treated.repoName(), treated.defaultBranch());
                List<CemProject.Commit> controlCommits = bareRepositoryDownloader.getCommits(control.repoOwner(), control.repoName(), control.defaultBranch());

                List<CemProject.Commit> treatedLinterCommits = getTreatedLinterCommits(allTreatedProjects.get(treated), treatedCommits);
                List<CemProject.Commit> controlLinterCommits = getControlLinterCommits(controlCommits, treatedCommits, treatedLinterCommits);

                try {
                    projectPairReaderWriter.write(new CemProjectPair(
                            new CemProject(
                                    treated.repoOwner(),
                                    treated.repoName(),
                                    treated.groupType(),
                                    treated.numberOfStars(),
                                    treated.creationYear(),
                                    treated.defaultBranch(),
                                    treatedCommits.size(),
                                    treatedLinterCommits
                            ),
                            new CemProject(
                                    control.repoOwner(),
                                    control.repoName(),
                                    control.groupType(),
                                    control.numberOfStars(),
                                    control.creationYear(),
                                    control.defaultBranch(),
                                    controlCommits.size(),
                                    controlLinterCommits // Insert newly calculated faked linter commit list
                            )));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private boolean pairExists(CemProject treated) {
        try {
            return projectPairReaderWriter.getElement(treated.repoOwner(), treated.repoName()).isPresent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private static List<CemProject.Commit> getTreatedLinterCommits(Proof treatedProof, List<CemProject.Commit> treatedCommits) {
        Set<String> treatedLinterCommitNames = treatedProof.linters().stream().map(Proof.LinterWithProof::introductionCommit).collect(Collectors.toSet());
        List<CemProject.Commit> treatedLinterCommits = new ArrayList<>();
        for (CemProject.Commit treatedCommit : treatedCommits) {
            if (treatedLinterCommitNames.contains(treatedCommit.name())) {
                treatedLinterCommits.add(treatedCommit);
            }
        }
        return treatedLinterCommits;
    }

    private String generateStratumKey(CemProject project) {
        // Group by number of stars
        int stars = project.numberOfStars();

        // Log scale: 4 bins between 10 and 100, 100 and 1000, 1000 and 10.000, ...
        String starBin;
        if (stars < 100) {
            starBin = "< 100";
        } else {
            double logValue = Math.log10(stars);

            double lowerBoundExp = Math.floor(logValue * NUMBER_OF_BUCKETS_PER_POWER_OF_TEN) / NUMBER_OF_BUCKETS_PER_POWER_OF_TEN;
            double upperBoundExp = lowerBoundExp + 1 / NUMBER_OF_BUCKETS_PER_POWER_OF_TEN;

            long lowerBound = Math.round(Math.pow(10, lowerBoundExp));
            long upperBound = Math.round(Math.pow(10, upperBoundExp));

            starBin = lowerBound + "-" + upperBound;
        }

        // Group by year
        int year = Integer.parseInt(project.creationYear());

        // combine to key
        return "STARS[" + starBin + "]_YEAR[" + year + "]";
    }
}