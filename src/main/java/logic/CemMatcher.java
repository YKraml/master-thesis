package logic;

import model.CemProject;
import model.CemProjectPair;
import util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CemMatcher {

    private static final double NUMBER_OF_BUCKETS_PER_POWER_OF_TEN = 4;

    public List<CemProjectPair> match(List<CemProject> allTreatedProjects, List<CemProject> allUntreatedProjects) {

        // Group ALL treated projects
        Map<String, List<CemProject>> treatedStrata = allTreatedProjects.stream()
                .collect(Collectors.groupingBy(this::generateStratumKey));

        // Group ALL control projects
        Map<String, List<CemProject>> controlStrata = allUntreatedProjects.stream()
                .collect(Collectors.groupingBy(this::generateStratumKey));

        List<CemProjectPair> finalPairs = new ArrayList<>();

        // Iterate over strata that exist in the Treated set
        for (String key : treatedStrata.keySet()) {

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

            for (int i = 0; i < numberOfMatches; i++) {
                CemProject treated = treatedBin.get(i);
                CemProject rawControl = controlsBin.get(i);

                // Create the adapted control twin (copying time windows from treated)
                CemProject adaptedControl = new CemProject(
                        rawControl.repoOwner(),
                        rawControl.repoName(),
                        rawControl.groupType(),
                        rawControl.numberOfStars(),
                        treated.analysisStartTime(), // Copy treated analysis start time
                        treated.analysisEndTime(),   // Copy treated analysis end time
                        rawControl.creationYear(),
                        rawControl.defaultBranch()
                );

                finalPairs.add(new CemProjectPair(treated, adaptedControl));
            }
        }

        return finalPairs;
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