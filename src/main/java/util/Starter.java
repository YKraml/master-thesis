package util;

import jakarta.inject.Inject;
import logic.RepositoryDownloader;
import logic.RepositoryHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Starter {

    private final GitHub github;
    private final RepositoryDownloader repositoryDownloader;
    private final RepositoryHandler repositoryHandler;
    private final ExecutorService executorService;
    private final State state;

    @Inject
    public Starter(GitHub github, RepositoryDownloader repositoryDownloader, RepositoryHandler repositoryHandler, ExecutorService executorService, State state) {
        this.github = github;
        this.repositoryDownloader = repositoryDownloader;
        this.repositoryHandler = repositoryHandler;
        this.executorService = executorService;
        this.state = state;
    }

    public void start() {
        state.startPrintingTimer();
        startAnalysis();
    }

    private void startAnalysis() {
        AtomicInteger maxNumberOfStars = new AtomicInteger(Integer.MAX_VALUE);
        AtomicReference<PagedIterator<GHRepository>> repos = new AtomicReference<>();

        Runnable task = () -> {
            setReposIfEmpty(repos, github, maxNumberOfStars);
            GHRepository repo = getNextRepo(repos);
            setMaxNumberOfStars(maxNumberOfStars, repo);

            try {
                repositoryHandler.handleRepo(repo);
            } catch (Exception e) {
                printError(e, repo);
                repositoryDownloader.deleteRepo(repo.getOwnerName(), repo.getName());
            } finally {
                state.removeFromState(Pair.of(repo.getOwnerName(), repo.getName()));
            }
        };

        for (int i = 0; i < Main.AVAILABLE_PROCESSORS; i++) {
            submitContinuousTask(task, executorService);
        }
    }

    private synchronized void setMaxNumberOfStars(AtomicInteger numberOfStars, GHRepository repo) {
        numberOfStars.set(repo.getStargazersCount());
    }

    private synchronized GHRepository getNextRepo(AtomicReference<PagedIterator<GHRepository>> repos) {
        return repos.get().next();
    }

    private synchronized void setReposIfEmpty(AtomicReference<PagedIterator<GHRepository>> repos, GitHub github, AtomicInteger numberOfStars) {
        if (repos.get() == null || !repos.get().hasNext()) {
            repos.set(github.searchRepositories().language("Java").stars("<=" + numberOfStars).sort(GHRepositorySearchBuilder.Sort.STARS).list().iterator());
        }
    }

    private void submitContinuousTask(Runnable task, Executor executorService) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(task, executorService);
        future.whenCompleteAsync((result, throwable) -> submitContinuousTask(task, executorService), executorService);
    }

    public synchronized void printError(Exception e, GHRepository next) {
        System.out.println();
        System.out.println(next.getHtmlUrl());
        System.out.println("Something went wrong!");
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
}
