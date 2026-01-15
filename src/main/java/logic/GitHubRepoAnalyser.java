package logic;

import io.json.GitHubRepositoryReaderWriter;
import jakarta.inject.Inject;
import model.GitHubRepository;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

public class GitHubRepoAnalyser {

    public static final int MAX_CONCURRENT_FUTURES = 32;
    private static final Semaphore SEMAPHORE = new Semaphore(MAX_CONCURRENT_FUTURES);

    private final GitRepositoryHandler gitRepositoryHandler;
    private final StateHandler stateHandler;
    private final GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter;
    private final ExecutorService executorService;

    @Inject
    public GitHubRepoAnalyser(GitRepositoryHandler gitRepositoryHandler, StateHandler stateHandler, GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter, ExecutorService executorService) {
        this.gitRepositoryHandler = gitRepositoryHandler;
        this.stateHandler = stateHandler;
        this.gitHubRepositoryReaderWriter = gitHubRepositoryReaderWriter;
        this.executorService = executorService;
    }

    public void start() throws IOException, InterruptedException {
        Iterator<GitHubRepository> gitHubRepositories = gitHubRepositoryReaderWriter.iterator();
        
        while (gitHubRepositories.hasNext()) {
            GitHubRepository gitHubRepository = gitHubRepositories.next();
            SEMAPHORE.acquire();

            String repoOwner = gitHubRepository.repoOwner();
            String repoName = gitHubRepository.repoName();

            CompletableFuture.runAsync(() -> {
                stateHandler.addToState(Pair.of(repoOwner, repoName), "Searching for linters");

                try {
                    gitRepositoryHandler.handleRepo(repoOwner, repoName);
                } catch (Exception e) {
                    System.out.println("Error processing " + repoOwner + "/" + repoName);
                    e.printStackTrace();
                }

                SEMAPHORE.release();
                stateHandler.removeFromState(Pair.of(repoOwner, repoName));
            }, executorService);
        }

        //Wait here, until loop is finished
        SEMAPHORE.acquire(MAX_CONCURRENT_FUTURES);
        SEMAPHORE.release(MAX_CONCURRENT_FUTURES);
    }
}
