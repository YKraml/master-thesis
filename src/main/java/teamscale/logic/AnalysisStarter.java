package teamscale.logic;

import com.google.inject.Inject;
import common.ResultReaderWriter;
import linter.searching.logic.State;
import org.apache.commons.lang3.tuple.Pair;
import teamscale.api.ProjectsGetter;
import teamscale.api.model.project.Project;
import teamscale.model.Result;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

import static common.ResultReaderWriter.*;

public class AnalysisStarter {

    private final static Semaphore SEMAPHORE = new Semaphore(4);

    private final ProjectsGetter projectsGetter;
    private final ExecutorService executorService;
    private final FindingsAnalyser findingsAnalyser;
    private final ResultReaderWriter resultReaderWriter;
    private final State state;

    @Inject
    public AnalysisStarter(ProjectsGetter projectsGetter, ExecutorService executorService, FindingsAnalyser findingsAnalyser, ResultReaderWriter resultReaderWriter, State state) {
        this.projectsGetter = projectsGetter;
        this.executorService = executorService;
        this.findingsAnalyser = findingsAnalyser;
        this.resultReaderWriter = resultReaderWriter;
        this.state = state;
    }

    public void startAnalysis() {
        for (Project project : projectsGetter.getExistingProjects()) {
            executorService.submit(() -> analyseProject(project));
        }
    }

    private void analyseProject(Project project) {
        String[] split = project.getName().split("\\.", 2);
        String repoOwner = split[0];
        String repoName = split[1];

        try {
            SEMAPHORE.acquire();
            state.addToState(Pair.of(repoOwner, repoName), "Analysing findings");
            if (!resultReaderWriter.resultFileIsPresent(repoOwner, repoName)) {
                Result result = findingsAnalyser.analyseFindings(repoOwner, repoName);
                resultReaderWriter.writeResult(result);
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        state.removeFromState(Pair.of(repoOwner, repoName));
        SEMAPHORE.release();
    }
}
