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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class DataCollector {

    public static final String FOLDER_NAME = "temp" + File.separator + "all";

    private final ProjectsGetter projectsGetter;
    private final State state;
    private final RepositoryHandler repositoryHandler;
    private final TeamscaleDataDownloader teamscaleDataDownloader;
    private final DataAnalyser dataAnalyser;
    private final ProofWithResultReaderWriter proofWithResultReaderWriter;

    @Inject
    public DataCollector(ProjectsGetter projectsGetter, State state, RepositoryHandler repositoryHandler, TeamscaleDataDownloader teamscaleDataDownloader, DataAnalyser dataAnalyser, ProofWithResultReaderWriter proofWithResultReaderWriter) {
        this.projectsGetter = projectsGetter;
        this.state = state;
        this.repositoryHandler = repositoryHandler;
        this.teamscaleDataDownloader = teamscaleDataDownloader;
        this.dataAnalyser = dataAnalyser;
        this.proofWithResultReaderWriter = proofWithResultReaderWriter;
    }

    public void startCollectingData() {
        List<Project> existingProjects = projectsGetter.getExistingProjects();

        for (Project existingProject : existingProjects) {
            String[] split = existingProject.getName().split("\\.", 2);
            String repoOwner = split[0];
            String repoName = split[1];

            state.addToState(Pair.of(repoOwner, repoName), "Prepare data for python");
            try {
                collectDataFor(repoOwner, repoName);
            } catch (GitAPIException | IOException | ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
            state.removeFromState(Pair.of(repoOwner, repoName));
        }
    }

    private void collectDataFor(String repoOwner, String repoName) throws GitAPIException, IOException, ExecutionException, InterruptedException, TimeoutException {
        Proof proof = repositoryHandler.handleRepo(repoOwner, repoName).get();
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom(repoOwner, repoName);
        Result result = dataAnalyser.analyseTeamscaleData(teamscaleData);

        ProofWithResult proofWithResult = new ProofWithResult(repoOwner, repoName, proof, result);
        proofWithResultReaderWriter.write(proofWithResult);
    }
}
