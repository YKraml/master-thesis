package logic;

import model.Linter;
import model.Proof;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    public static final String TOKEN = "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";

    private final static List<Pair<String, String>> repos = List.of(
            Pair.of("YKraml", "KramlsBot"),
            Pair.of("YKraml", "GameOfLive"),
            Pair.of("YKraml", "MandelBrot"),
            Pair.of("YKraml", "AnimeFetcher"),
            Pair.of("YKraml", "WaveFunctionCollapse"),
            Pair.of("YKraml", "InformatiCup2023")
    );

    public static void main(String[] args) throws IOException, GitAPIException {

        GitHub github = new GitHubBuilder().withOAuthToken(TOKEN).build();
        System.out.printf("Remaining: %s%n", github.getRateLimit().getCore().getLimit());;

        RepositoryDownloader repositoryDownloader = new RepositoryDownloader(github);
        LinterFolderFinder linterFolderFinder = new LinterFolderFinder(new LinterFileFinder());
        LinterIntroductionFinder linterIntroductionFinder = new LinterIntroductionFinder(linterFolderFinder);

        for (Pair<String, String> repo : repos) {

            System.out.println();
            System.out.println(repo);
            try (Git git = repositoryDownloader.getRepo(repo.getKey(), repo.getValue())) {

                File folder = git.getRepository().getDirectory().getParentFile();
                Map<Linter, Proof> linters = linterFolderFinder.findLinters(folder);
                System.out.println(linters);


                for (Map.Entry<Linter, Proof> linterProofEntry : linters.entrySet()) {
                    Linter linter = linterProofEntry.getKey();
                    Proof proof = linterProofEntry.getValue();

                    String commitId = linterIntroductionFinder.findIntroduction(git, linter).getName();
                    System.out.println(linter + " Introduced: " + commitId);

                }


            }
        }


    }


}
