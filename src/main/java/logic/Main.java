package logic;

import model.Linter;
import model.Proof;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static final String TOKEN = "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";

    public static void main(String[] args) throws IOException, GitAPIException {
        GitHub github = new GitHubBuilder().withOAuthToken(TOKEN).build();

        System.out.printf("Remaining: %s%n", github.getRateLimit().getCore().getLimit());

        RepositoryDownloader repositoryDownloader = new RepositoryDownloader(github);
        LinterFolderFinder linterFolderFinder = new LinterFolderFinder(new LinterFileFinder());
        LinterIntroductionFinder linterIntroductionFinder = new LinterIntroductionFinder(linterFolderFinder);


        PagedSearchIterable<GHRepository> repos = github.searchRepositories().language("Java").sort(GHRepositorySearchBuilder.Sort.STARS).list();
        List<GHRepository> repoList = new ArrayList<>();
        for (GHRepository ghRepository : repos) {
            if (repoList.size() == 100) {
                break;
            }
            repoList.add(ghRepository);
        }

        for (GHRepository repo : repoList) {
            handleRepo(repo, repositoryDownloader, linterFolderFinder, linterIntroductionFinder);
        }


    }

    private static void handleRepo(GHRepository repo, RepositoryDownloader repositoryDownloader, LinterFolderFinder linterFolderFinder, LinterIntroductionFinder linterIntroductionFinder) throws IOException, GitAPIException {
        String ownerName = repo.getOwnerName();
        String repoName = repo.getName();

        System.out.println();
        System.out.println("Ownber: " + ownerName);
        System.out.println("Repo: " + repoName);

        File proofFile = new File("proofs" + File.separator + ownerName + "." + repoName + ".txt");
        if (proofFile.exists()) {
            System.out.println("Skipping");
            return;
        }

        try (Git git = repositoryDownloader.getRepo(ownerName, repoName)) {


            File folder = git.getRepository().getDirectory().getParentFile();
            Map<Linter, Proof> linters = linterFolderFinder.findLinters(folder);
            System.out.println("Linters: " + linters);


            StringBuilder text = new StringBuilder();
            for (Map.Entry<Linter, Proof> linterProofEntry : linters.entrySet()) {
                Linter linter = linterProofEntry.getKey();
                Proof proof = linterProofEntry.getValue();

                String commitId = linterIntroductionFinder.findIntroduction(git, linter).getName();
                text.append(linter)
                        .append("\n")
                        .append(proof)
                        .append("\n")
                        .append(commitId)
                        .append("\n");

                System.out.println(linter + " Introduced: " + commitId);
            }

            if (!proofFile.getParentFile().exists()) {
                proofFile.getParentFile().mkdirs();
            }
            if (!proofFile.exists()) {
                proofFile.createNewFile();
            }
            Files.writeString(proofFile.toPath(), text.toString());


        }
    }


}
