package logic;

import api.ProjectsGetter;
import api.model.cleaned.TeamscaleData;
import api.model.project.Project;
import com.google.inject.Inject;
import io.ProofWithResultReaderWriter;
import io.TeamscaleDataDownloader;
import model.Proof;
import model.ProofWithResult;
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

    private final ProjectsGetter projectsGetter;
    private final State state;
    private final RepositoryHandler repositoryHandler;
    private final TeamscaleDataDownloader teamscaleDataDownloader;
    private final DataAnalyser dataAnalyser;
    private final ProofWithResultReaderWriter proofWithResultReaderWriter;
    private final ExecutorService executorService;

    @Inject
    public DataCollector(ProjectsGetter projectsGetter, State state, RepositoryHandler repositoryHandler, TeamscaleDataDownloader teamscaleDataDownloader, DataAnalyser dataAnalyser, ProofWithResultReaderWriter proofWithResultReaderWriter, ExecutorService executorService) {
        this.projectsGetter = projectsGetter;
        this.state = state;
        this.repositoryHandler = repositoryHandler;
        this.teamscaleDataDownloader = teamscaleDataDownloader;
        this.dataAnalyser = dataAnalyser;
        this.proofWithResultReaderWriter = proofWithResultReaderWriter;
        this.executorService = executorService;
    }

    public void startCollectingData() {
        List<Project> existingProjects = projectsGetter.getExistingProjects();

        // First collect all proofs
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<Proof> proofs = Collections.synchronizedList(new ArrayList<>());
        for (Project existingProject : existingProjects) {
            futures.add(CompletableFuture.runAsync(() -> {
                String[] split = existingProject.getName().split("\\.", 2);
                String repoOwner = split[0];
                String repoName = split[1];

                state.addToState(Pair.of(repoOwner, repoName), "Prepare data for python");
                try {
                    proofs.add(repositoryHandler.handleRepo(repoOwner, repoName));
                } catch (GitAPIException | IOException e) {
                    e.printStackTrace();
                }
                state.removeFromState(Pair.of(repoOwner, repoName));
            }, executorService));
        }

        // Wait
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect data from teamscale
        for (Proof proof : proofs) {
            String repoOwner = proof.repoOwner();
            String repoName = proof.repoName();
            state.addToState(Pair.of(repoOwner, repoName), "Start collecting data from Teamscale");
            try {
                collectDataFor(proof);
            } catch (GitAPIException | IOException | ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
            state.removeFromState(Pair.of(repoOwner, repoName));
        }

    }

    private void collectDataFor(Proof proof) throws GitAPIException, IOException, ExecutionException, InterruptedException, TimeoutException {
        String repoOwner = proof.repoOwner();
        String repoName = proof.repoName();
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom(repoOwner, repoName);
        Result result = dataAnalyser.analyseTeamscaleData(teamscaleData);

        ProofWithResult proofWithResult = new ProofWithResult(repoOwner, repoName, proof, result);
        proofWithResultReaderWriter.write(proofWithResult);
    }
}
