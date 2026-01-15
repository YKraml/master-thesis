package logic;

import com.google.inject.Inject;
import io.json.CombinedDataReaderWriter;
import io.json.ProofReaderWriter;
import io.teamscale.TeamscaleDataDownloader;
import io.teamscale.api.ProjectsFetcher;
import io.teamscale.api.model.cleaned.TeamscaleData;
import io.teamscale.api.model.project.Project;
import model.CombinedData;
import model.Proof;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class DataCollector {

    private final StateHandler stateHandler;
    private final GitHubRepoAnalyser gitHubRepoAnalyser;
    private final ExecutorService executorService;

    private final ProofReaderWriter proofReaderWriter;
    private final CombinedDataReaderWriter combinedDataReaderWriter;

    private final TeamscaleDataDownloader teamscaleDataDownloader;
    private final ProjectsFetcher projectsFetcher;
    private final DataCombiner dataCombiner;

    @Inject
    public DataCollector(ProjectsFetcher projectsFetcher, StateHandler stateHandler, GitHubRepoAnalyser gitHubRepoAnalyser, TeamscaleDataDownloader teamscaleDataDownloader, DataCombiner dataCombiner, CombinedDataReaderWriter combinedDataReaderWriter, ExecutorService executorService, ProofReaderWriter proofReaderWriter) {
        this.projectsFetcher = projectsFetcher;
        this.stateHandler = stateHandler;
        this.gitHubRepoAnalyser = gitHubRepoAnalyser;
        this.teamscaleDataDownloader = teamscaleDataDownloader;
        this.dataCombiner = dataCombiner;
        this.combinedDataReaderWriter = combinedDataReaderWriter;
        this.executorService = executorService;
        this.proofReaderWriter = proofReaderWriter;
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

                stateHandler.addToState(Pair.of(repoOwner, repoName), "Getting proof");
                try {
                    proofs.add(gitHubRepoAnalyser.handleRepo(repoOwner, repoName));
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
                CombinedData combinedData = collectDataFor(repoOwner, repoName);
                combinedDataReaderWriter.write(combinedData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            stateHandler.removeFromState(Pair.of(repoOwner, repoName));
        }
    }

    private CombinedData collectDataFor(String repoOwner, String repoName) throws Exception {
        Proof proof = proofReaderWriter.getElement(repoOwner, repoName).orElseThrow();
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom(repoOwner, repoName);
        return dataCombiner.combineData(proof, teamscaleData);
    }
}
