package logic;

import api.ProjectsFetcher;
import api.model.cleaned.TeamscaleData;
import api.model.project.Project;
import com.google.inject.Inject;
import io.CombinedDataReaderWriter;
import io.TeamscaleDataDownloader;
import model.CombinedData;
import model.Proof;
import model.Result;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public class DataCollector {

    private final ProjectsFetcher projectsFetcher;
    private final StateHandler stateHandler;
    private final GitRepositoryHandler gitRepositoryHandler;
    private final TeamscaleDataDownloader teamscaleDataDownloader;
    private final DataAnalyser dataAnalyser;
    private final CombinedDataReaderWriter combinedDataReaderWriter;
    private final ExecutorService executorService;

    @Inject
    public DataCollector(ProjectsFetcher projectsFetcher, StateHandler stateHandler, GitRepositoryHandler gitRepositoryHandler, TeamscaleDataDownloader teamscaleDataDownloader, DataAnalyser dataAnalyser, CombinedDataReaderWriter combinedDataReaderWriter, ExecutorService executorService) {
        this.projectsFetcher = projectsFetcher;
        this.stateHandler = stateHandler;
        this.gitRepositoryHandler = gitRepositoryHandler;
        this.teamscaleDataDownloader = teamscaleDataDownloader;
        this.dataAnalyser = dataAnalyser;
        this.combinedDataReaderWriter = combinedDataReaderWriter;
        this.executorService = executorService;
    }

    public void startCollectingData() {
        List<Project> existingProjects = projectsFetcher.getExistingProjects();

        // First collect all proofs
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<Proof> proofs = Collections.synchronizedList(new ArrayList<>());
        for (Project existingProject : existingProjects) {
            futures.add(CompletableFuture.runAsync(() -> {
                String[] split = existingProject.getName().split("\\.", 2);
                String repoOwner = split[0];
                String repoName = split[1];

                stateHandler.addToState(Pair.of(repoOwner, repoName), "Prepare data for python");
                try {
                    proofs.add(gitRepositoryHandler.handleRepo(repoOwner, repoName));
                } catch (GitAPIException | IOException e) {
                    e.printStackTrace();
                }
                stateHandler.removeFromState(Pair.of(repoOwner, repoName));
            }, executorService));
        }

        // Wait
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect data for each project
        for (Proof proof : proofs) {
            String repoOwner = proof.repoOwner();
            String repoName = proof.repoName();
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Start collecting data");
            try {
                collectDataFor(proof);
            } catch (GitAPIException | IOException | ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
            stateHandler.removeFromState(Pair.of(repoOwner, repoName));
        }

    }

    private CombinedData collectDataFor(Proof proof) throws GitAPIException, IOException, ExecutionException, InterruptedException, TimeoutException {
        String repoOwner = proof.repoOwner();
        String repoName = proof.repoName();

        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom(repoOwner, repoName);
        Result result = dataAnalyser.analyseTeamscaleData(teamscaleData);

        CombinedData combinedData = new CombinedData(repoOwner, repoName, proof, result);
        combinedDataReaderWriter.write(combinedData);

        return combinedData;
    }
}
