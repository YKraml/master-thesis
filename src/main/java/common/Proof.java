package common;

import linter.searching.model.Language;

import java.util.List;

public record Proof(String repoOwner, String repoName, Language language, String link, String defaultBranch,
                    String currentCommit, int currentTime, int analysisStartTime, int analysisEndTime,
                    List<LinterWithProof> linters) {

    public record LinterWithProof(String linterName, String currentFile,
                                  String introductionCommit, String introductionFile, int introductionTime) {

    }
}