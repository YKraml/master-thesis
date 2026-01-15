package model;

public record GitHubRepository(
        String repoOwner,
        String repoName,
        int numberOfStars,
        String defaultBranch,
        int size
) implements ProjectWritable {
}
