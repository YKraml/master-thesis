import api.ProjectsGetter;
import api.model.cleaned.TeamscaleData;
import api.model.project.Project;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.CsvReaderWriter;
import io.TeamscaleDataDownloader;
import logic.RepositoryHandler;
import logic.State;
import model.Proof;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import util.GuiceModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class MainPreparePythonScript {

    private final ProjectsGetter projectsGetter;
    private final CsvReaderWriter csvReaderWriter;
    private final State state;
    private final RepositoryHandler repositoryHandler;
    private final TeamscaleDataDownloader teamscaleDataDownloader;
    private final ExecutorService executorService;

    @Inject
    public MainPreparePythonScript(ProjectsGetter projectsGetter, CsvReaderWriter csvReaderWriter, State state, RepositoryHandler repositoryHandler, TeamscaleDataDownloader teamscaleDataDownloader, ExecutorService executorService) {
        this.projectsGetter = projectsGetter;
        this.csvReaderWriter = csvReaderWriter;
        this.state = state;
        this.repositoryHandler = repositoryHandler;
        this.teamscaleDataDownloader = teamscaleDataDownloader;
        this.executorService = executorService;
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(State.class).start();
        injector.getInstance(MainPreparePythonScript.class).prepareData();
        injector.getInstance(State.class).end();
    }

    private void prepareData() {
        List<Project> existingProjects = projectsGetter.getExistingProjects();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Project existingProject : existingProjects) {
            String[] split = existingProject.getName().split("\\.", 2);
            String repoOwner = split[0];
            String repoName = split[1];

            futures.add(CompletableFuture.runAsync(() -> {
                state.addToState(Pair.of(repoOwner, repoName), "Prepare data for python");
                try {
                    prepareData(repoOwner, repoName);
                } catch (GitAPIException | IOException e) {
                    e.printStackTrace();
                }
                state.removeFromState(Pair.of(repoOwner, repoName));
            }, executorService));

        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void prepareData(String repoOwner, String repoName) throws GitAPIException, IOException {

        Proof proof = repositoryHandler.handleRepo(repoOwner, repoName).get();
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom(repoOwner, repoName);

        //TODO: continue here

    }
}
