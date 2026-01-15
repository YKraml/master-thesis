package model;

import java.util.List;

public record CemProject(String repoOwner,
                         String repoName,
                         GroupType groupType,
                         int numberOfStars,
                         List<Double> ageAtLinterIntros,
                         String creationYear) implements ProjectWritable {

    public enum GroupType {
        TREATMENT,
        CONTROL
    }
}
