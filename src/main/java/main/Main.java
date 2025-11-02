package main;

import logic.*;
import org.apache.commons.lang3.tuple.Pair;
import org.kohsuke.github.*;
import util.State;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static final String TOKEN = "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";
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(TOKEN).build();
        System.out.printf("Remaining: %s%n", github.getRateLimit().getCore().getLimit());

        RepositoryDownloader repositoryDownloader = new RepositoryDownloader(github);
        LinterFinder linterFinder = new LinterFinder();
        LinterIntroductionFinder linterIntroductionFinder = new LinterIntroductionFinderDiff(linterFinder);
        RepoHandler repoHandler = new RepoHandler();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                State.getInstance().printState();
            }
        }, 1000, 1000);

        AtomicInteger maxNumberOfStars = new AtomicInteger(Integer.MAX_VALUE);
        AtomicReference<PagedIterator<GHRepository>> repos = new AtomicReference<>();
        setReposIfEmpty(repos, github, maxNumberOfStars);

        Runnable task = () -> {
            setReposIfEmpty(repos, github, maxNumberOfStars);
            GHRepository repo = getNextRepo(repos);
            setMaxNumberOfStars(maxNumberOfStars, repo);

            try {
                repoHandler.handleRepo(repo, repositoryDownloader, linterFinder, linterIntroductionFinder);
            } catch (Exception e) {
                printError(e, repo);
                repositoryDownloader.deleteRepo(repo.getOwnerName(), repo.getName());
            } finally {
                State.getInstance().removeFromState(Pair.of(repo.getOwnerName(), repo.getName()));
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
        for (int i = 0; i < AVAILABLE_PROCESSORS; i++) {
            submitContinuousTask(task, executorService);
        }
    }

    private synchronized static void setMaxNumberOfStars(AtomicInteger numberOfStars, GHRepository repo) {
        numberOfStars.set(repo.getStargazersCount());
    }

    private synchronized static GHRepository getNextRepo(AtomicReference<PagedIterator<GHRepository>> repos) {
        return repos.get().next();
    }

    private synchronized static void setReposIfEmpty(AtomicReference<PagedIterator<GHRepository>> repos, GitHub github, AtomicInteger numberOfStars) {
        if (repos.get() == null || !repos.get().hasNext()) {
            repos.set(github.searchRepositories().language("Java").stars("<" + numberOfStars).sort(GHRepositorySearchBuilder.Sort.STARS).list().iterator());
        }
    }

    private static void submitContinuousTask(Runnable task, Executor executorService) {
        CompletableFuture.runAsync(task, executorService)
                .whenCompleteAsync((result, throwable) -> submitContinuousTask(task, executorService), executorService);
    }

    public synchronized static void printError(Exception e, GHRepository next) {
        System.out.println();
        System.out.println(next.getHtmlUrl());
        System.out.println("Something went wrong!");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
}
