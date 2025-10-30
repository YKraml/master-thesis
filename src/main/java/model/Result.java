package model;

import java.util.List;

public class Result {

    private final String repoOwner;
    private final String repoName;
    private final Language language;
    private final String link;
    private final List<LinterWithProof> linters;

    public Result(String repoOwner, String repoName, Language language, String link, List<LinterWithProof> linters) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.language = language;
        this.link = link;
        this.linters = linters;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public String getRepoName() {
        return repoName;
    }

    public List<LinterWithProof> getLinters() {
        return linters;
    }

    public Language getLanguage() {
        return language;
    }

    public String getLink() {
        return link;
    }

    public static class LinterWithProof {

        private final String linterName;
        private final String currentFile;
        private final String currentCommit;
        private final String introductionFile;
        private final String introductionCommit;

        public LinterWithProof(String linterName, String currentFile, String commit, String introductionCommit, String introductionFile) {
            this.linterName = linterName;
            this.currentFile = currentFile;
            this.currentCommit = commit;
            this.introductionCommit = introductionCommit;
            this.introductionFile = introductionFile;
        }

        public String getLinterName() {
            return linterName;
        }

        public String getCurrentFile() {
            return currentFile;
        }

        public String getCurrentCommit() {
            return currentCommit;
        }

        public String getIntroductionCommit() {
            return introductionCommit;
        }

        public String getIntroductionFile() {
            return introductionFile;
        }

        @Override
        public String toString() {
            return "LinterWithProof{" +
                    "linterName='" + linterName + '\'' +
                    ", currentFile='" + currentFile + '\'' +
                    ", currentCommit='" + currentCommit + '\'' +
                    ", introductionFile='" + introductionFile + '\'' +
                    ", introductionCommit='" + introductionCommit + '\'' +
                    '}';
        }
    }
}
