package model;

public record CemProject(String repoOwner,
                         String repoName,
                         GroupType groupType,
                         int numberOfStars,
                         int analysisStartTime,
                         int analysisEndTime,
                         String creationYear,
                         String defaultBranch) implements ProjectWritable {

    public enum GroupType {
        TREATMENT,
        CONTROL
    }
}
