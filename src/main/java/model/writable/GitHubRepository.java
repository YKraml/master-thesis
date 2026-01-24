package model.writable;

public record GitHubRepository(
        String repoOwner,
        String repoName,
        int numberOfStars,
        String defaultBranch,
        int size
) implements Writable {
}
