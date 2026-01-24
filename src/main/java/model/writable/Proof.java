package model.writable;

import model.linting.Language;

import java.util.List;

public record Proof(
        String repoOwner,
        String repoName,
        Language language,
        String link,
        String defaultBranch,
        String currentCommit,
        int currentCommitTime,
        String firstCommit,
        int firstCommitTime,
        int analysisStartTime,
        int analysisEndTime,
        int numberOfStars,
        List<LinterWithProof> linters) implements Writable {

    public record LinterWithProof(
            String linterName,
            String currentFile,
            String introductionCommit,
            String introductionFile,
            int introductionTime) {
    }
}