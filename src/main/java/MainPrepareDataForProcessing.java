import api.ProjectsGetter;
import api.model.cleaned.TeamscaleData;
import api.model.project.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.TeamscaleDataDownloader;
import logic.DataAnalyser;
import logic.RepositoryHandler;
import logic.State;
import model.Proof;
import model.ProofWithResult;
import model.Result;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import util.GuiceModule;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class MainPrepareDataForProcessing {

    private final ProjectsGetter projectsGetter;
    private final State state;
    private final RepositoryHandler repositoryHandler;
    private final TeamscaleDataDownloader teamscaleDataDownloader;
    private final DataAnalyser dataAnalyser;

    @Inject
    public MainPrepareDataForProcessing(ProjectsGetter projectsGetter, State state, RepositoryHandler repositoryHandler, TeamscaleDataDownloader teamscaleDataDownloader, DataAnalyser dataAnalyser) {
        this.projectsGetter = projectsGetter;
        this.state = state;
        this.repositoryHandler = repositoryHandler;
        this.teamscaleDataDownloader = teamscaleDataDownloader;
        this.dataAnalyser = dataAnalyser;
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(State.class).start();
        injector.getInstance(MainPrepareDataForProcessing.class).prepareData();
        injector.getInstance(State.class).end();
    }

    private void prepareData() {
        List<Project> existingProjects = projectsGetter.getExistingProjects();

        for (Project existingProject : existingProjects) {
            String[] split = existingProject.getName().split("\\.", 2);
            String repoOwner = split[0];
            String repoName = split[1];

            state.addToState(Pair.of(repoOwner, repoName), "Prepare data for python");
            try {
                prepareData(repoOwner, repoName);
            } catch (GitAPIException | IOException | ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }
            state.removeFromState(Pair.of(repoOwner, repoName));
        }
    }

    private void prepareData(String repoOwner, String repoName) throws GitAPIException, IOException, ExecutionException, InterruptedException, TimeoutException {
        Proof proof = repositoryHandler.handleRepo(repoOwner, repoName).get();

        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom(repoOwner, repoName);
        Result result = dataAnalyser.analyseTeamscaleData(teamscaleData);

        new File("temp" + File.separator + "all").mkdirs();

        File file = new File("temp" + File.separator + "all" + File.separator + repoOwner + "." + repoName + ".json");
        ProofWithResult proofWithResult = new ProofWithResult(proof, result);
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, proofWithResult);
    }
}
