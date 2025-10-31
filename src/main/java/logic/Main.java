package logic;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static final String TOKEN = "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";
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();


    public static void main(String[] args) throws IOException, GitAPIException {
        GitHub github = new GitHubBuilder().withOAuthToken(TOKEN).build();
        System.out.printf("Remaining: %s%n", github.getRateLimit().getCore().getLimit());

        RepositoryDownloader repositoryDownloader = new RepositoryDownloader(github);
        LinterFolderFinder linterFolderFinder = new LinterFolderFinder(new LinterFileFinder());
        LinterIntroductionFinder linterIntroductionFinder = new LinterIntroductionFinderDiff(linterFolderFinder);
        RepoHandler repoHandler = new RepoHandler();

        PagedIterator<GHRepository> repos = github.searchRepositories().language("Java").sort(GHRepositorySearchBuilder.Sort.STARS).list().iterator();

        Runnable task = () -> {
            GHRepository next;
            synchronized (repos) {
                next = repos.next();
            }

            try {
                repoHandler.handleRepo(next, repositoryDownloader, linterFolderFinder, linterIntroductionFinder);
            } catch (Exception e) {
                Printer.getInstance().printError(e, next);
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
        for (int i = 0; i < AVAILABLE_PROCESSORS; i++) {
            submitContinuousTask(task, executorService);
        }

    }

    private static void submitContinuousTask(Runnable task, Executor executorService) {
        CompletableFuture.runAsync(task, executorService)
                .whenCompleteAsync((result, throwable) -> submitContinuousTask(task, executorService), executorService);
    }

}
