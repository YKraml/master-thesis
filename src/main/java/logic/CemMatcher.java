package logic;

import model.CemProject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class CemMatcher {

    private static final double NUMBER_OF_BUCKETS_PER_POWER_OF_TEN = 4;
    private static final Random RANDOM = new Random(42);

    public List<CemProject> match(List<CemProject> treatedProjects, List<CemProject> candidatePool) {

        // Group treated projects
        Map<String, List<CemProject>> treatedStrata = treatedProjects.stream().collect(Collectors.groupingBy(this::generateStratumKey));

        // Group control projects
        Map<String, List<CemProject>> controlStrata = candidatePool.stream().collect(Collectors.groupingBy(this::generateStratumKey));

        List<CemProject> result = new ArrayList<>();
        for (String key : treatedStrata.keySet()) {
            List<CemProject> controlsInBin = controlStrata.get(key);
            List<CemProject> treatedInBin = treatedStrata.get(key);

            for (CemProject treated : treatedInBin) {

                // Get random control with same key
                CemProject controlTwin;
                try {
                    controlTwin = controlsInBin.get(RANDOM.nextInt(controlsInBin.size()));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("A twin could not be found for:" + key);
                }
                controlsInBin.remove(controlTwin);

                //Create control project and add to list
                result.add(new CemProject(
                        controlTwin.id(),
                        controlTwin.groupType(),
                        controlTwin.numberOfStars(),
                        treated.ageAtLinterIntro(),
                        controlTwin.creationYear())
                );
            }
        }

        return result;
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