package model;

import java.util.List;

public class Result {

    private final String repoOwner;
    private final String repoName;
    private final Language language;
    private final String link;
    private final int stars;
    private final List<LinterWithProof> linters;

    public Result(String repoOwner, String repoName, Language language, String link, int stars, List<LinterWithProof> linters) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.language = language;
        this.link = link;
        this.stars = stars;
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

    public int getStars() {
        return stars;
    }

    public record LinterWithProof(String linterName, String currentFile, String currentCommit,
                                  String introductionCommit, String introductionFile) {

    }
}