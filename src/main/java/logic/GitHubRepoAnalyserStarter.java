package logic;

import io.json.GitHubRepositoryReaderWriter;
import jakarta.inject.Inject;
import model.GitHubRepository;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class GitHubRepoAnalyserStarter {

    private final GitHubRepoAnalyser gitHubRepoAnalyser;
    private final StateHandler stateHandler;
    private final GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter;
    private final ExecutorService executorService;

    @Inject
    public GitHubRepoAnalyserStarter(GitHubRepoAnalyser gitHubRepoAnalyser, StateHandler stateHandler, GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter, ExecutorService executorService) {
        this.gitHubRepoAnalyser = gitHubRepoAnalyser;
        this.stateHandler = stateHandler;
        this.gitHubRepositoryReaderWriter = gitHubRepositoryReaderWriter;
        this.executorService = executorService;
    }

    public void start() throws IOException, InterruptedException {
        stateHandler.addToState(Pair.of("IO", ""), "Reading GitHub Project files");
        List<GitHubRepository> gitHubRepositories = gitHubRepositoryReaderWriter.readAll();
        gitHubRepositories.sort((o1, o2) -> o2.numberOfStars() - o1.numberOfStars());
        stateHandler.removeFromState(Pair.of("IO", ""));

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (GitHubRepository gitHubRepository : gitHubRepositories) {

            String repoOwner = gitHubRepository.repoOwner();
            String repoName = gitHubRepository.repoName();

            futures.add(CompletableFuture.runAsync(() -> {
                stateHandler.addToState(Pair.of(repoOwner, repoName), "Searching for linters");

                try {
                    gitHubRepoAnalyser.handleRepo(repoOwner, repoName);
                } catch (Exception e) {
                    System.out.println("Error processing " + repoOwner + "/" + repoName);
                    e.printStackTrace();
                }

                stateHandler.removeFromState(Pair.of(repoOwner, repoName));
            }, executorService));
        }

        //Wait here, until loop is finished
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
    }
}
