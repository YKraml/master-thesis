package logic;

import model.Result;
import org.kohsuke.github.GHRepository;

public class Printer {

    private final static Printer INSTANCE = new Printer();

    private Printer() {
    }

    public static Printer getInstance() {
        return INSTANCE;
    }

    public void printError(Exception e, GHRepository next) {
        synchronized (this) {
            System.out.println();
            System.out.println(next.getHtmlUrl());
            System.out.println("Something went wrong!");
            e.printStackTrace();
        }
    }

    public void printFinishedAnalysing(Result result) {
        synchronized (this) {
            System.out.println();
            System.out.println("Finished analysing:");
            System.out.println(result.getLink());
            System.out.println("Owner: " + result.getRepoOwner());
            System.out.println("Repo: " + result.getRepoName());
            System.out.println("Linters: " + result.getLinters());

        }
    }

    public void printFolderTooLarge(String ownerName, String repoName, long size) {
        synchronized (this) {
            System.out.println();
            System.out.println("https://github.com/" + ownerName + "/" + repoName);
            System.out.println("Skipping. Folder too large: " + size / 1000 + " kB");
        }
    }

    public void printAlreadyAnalysed(String ownerName, String repoName) {
        synchronized (this) {
            System.out.println();
            System.out.println("https://github.com/" + ownerName + "/" + repoName);
            System.out.println("Skipping. Already analysed.");
        }
    }

    public void printRepoTooLarge(GHRepository repo, String ownerName, String repoName) {
        synchronized (this) {
            System.out.println();
            System.out.println("https://github.com/" + ownerName + "/" + repoName);
            System.out.println("Skipping. Repo too large: " + repo.getSize() + " kB");
        }
    }

    public void printStartAnalysing(String ownerName, String repoName) {
        synchronized (this) {
            System.out.println();
            System.out.println("https://github.com/" + ownerName + "/" + repoName);
            System.out.println("Start analysing.");
        }
    }
}
