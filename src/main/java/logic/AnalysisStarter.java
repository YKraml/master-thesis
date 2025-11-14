package logic;

import api.ProjectsGetter;
import api.model.project.Project;
import com.google.inject.Inject;
import io.ResultReaderWriter;
import model.Result;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class AnalysisStarter {

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

    public void startAnalysis() throws IOException {
        for (Project project : projectsGetter.getExistingProjects()) {
            analyseProject(project);
        }
    }

    private void analyseProject(Project project) throws IOException {
        String[] split = project.getName().split("\\.", 2);
        String repoOwner = split[0];
        String repoName = split[1];

        state.addToState(Pair.of(repoOwner, repoName), "Analysing findings");
        if (!resultReaderWriter.resultFileIsPresent(repoOwner, repoName)) {
            Result result = findingsAnalyser.analyseFindings(repoOwner, repoName);
            resultReaderWriter.writeResult(result);
        }

        state.removeFromState(Pair.of(repoOwner, repoName));
    }
}
