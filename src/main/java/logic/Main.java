package logic;

import model.Language;
import model.Linter;
import model.Proof;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        for (GHRepository ghRepository : repos) {
            handleRepo(ghRepository, repositoryDownloader, linterFolderFinder, linterIntroductionFinder);
        }

    }

    private static void handleRepo(GHRepository repo, RepositoryDownloader repositoryDownloader, LinterFolderFinder linterFolderFinder, LinterIntroductionFinder linterIntroductionFinder) throws IOException, GitAPIException {
        String ownerName = repo.getOwnerName();
        String repoName = repo.getName();

        System.out.println();
        System.out.println("https://github.com/" + ownerName + "/" + repoName);
        System.out.println("Owner: " + ownerName);
        System.out.println("Repo: " + repoName);
        System.out.println("Repo Size: " + repo.getSize() + " kb");

        //Limit at around 0.5GB. The getSize() is not very accurate.
        if (repo.getSize() > 500000) {
            System.out.println("Skipping. Repo too large.");
            return;
        }

        //Skip if already analysed
        File proofFile = new File("proofs" + File.separator + ownerName + "." + repoName + ".txt");
        if (proofFile.exists()) {
            System.out.println("Skipping. Already analysed.");
            return;
        }

        try (Git git = repositoryDownloader.getRepo(ownerName, repoName)) {

            File folder = git.getRepository().getDirectory().getParentFile();
            long size = Files.walk(folder.toPath())
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();

            System.out.println("Real Size: " + size / 1000 + " kB");

            //Limit at 0.5GB. The getSize() is not very accurate.
            if (size > 500 * 1000 * 1000) {
                System.out.println("Skipping. Folder too large.");
                return;
            }

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
