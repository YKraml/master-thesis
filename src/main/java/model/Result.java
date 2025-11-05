package model;

import java.util.List;

public record Result(String repoOwner, String repoName, Language language, String link, String defaultBranch,
                     List<LinterWithProof> linters) {

    public record LinterWithProof(String linterName, String currentCommit, String currentFile, int currentTime,
                                  String introductionCommit, String introductionFile, int introductionTime) {

    }
}