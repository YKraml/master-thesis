package model;

import java.util.List;

public class Result {

    private final String repoOwner;
    private final String repoName;
    private final List<LinterWithProof> linters;

    public Result(String repoOwner, String repoName, List<LinterWithProof> linters) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
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

    public static class LinterWithProof {

        private final Linter linter;
        private final Proof proof;
        private final String commit;

        private LinterWithProof(Linter linter, Proof proof, String commit) {
            this.linter = linter;
            this.proof = proof;
            this.commit = commit;
        }

        public Linter getLinter() {
            return linter;
        }

        public Proof getProof() {
            return proof;
        }

        public String getCommit() {
            return commit;
        }
    }
}
