package model;

public record CombinedData(String repoOwner, String repoName, Proof proof, Result result) implements Writable {

}
