package model;

public record ProofWithResult(String repoOwner, String repoName, Proof proof, Result result) implements Writable {

}
