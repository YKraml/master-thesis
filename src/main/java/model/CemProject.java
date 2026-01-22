package model;

import java.util.List;

public record CemProject(
        String repoOwner,
        String repoName,
        GroupType groupType,
        int numberOfStars,
        String creationYear,
        String defaultBranch,
        int numberOfCommits,
        int loc,
        int sloc,
        List<LinterCommit> linterCommits,
        List<Commit> allCommits) implements ProjectWritable {

    public enum GroupType {
        TREATMENT,
        CONTROL
    }

    public record Commit(String name, int time, int index, String user) {
    }

    public record LinterCommit(String name, int time, int index, String user, String linterName,
                               int numberOfCommitsInTwoYearWindow) {
    }
}
