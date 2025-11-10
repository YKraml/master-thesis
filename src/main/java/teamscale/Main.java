package teamscale;

import com.google.inject.Guice;
import com.google.inject.Injector;
import common.GuiceModule;
import common.ResultReaderWriter;
import linter.searching.logic.State;
import org.eclipse.jgit.api.errors.GitAPIException;
import teamscale.logic.FindingsAnalyser;
import teamscale.model.Result;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, GitAPIException {
        Injector injector = Guice.createInjector(new GuiceModule());
        //injector.getInstance(ProjectCreator.class).createAllProjects();

        FindingsAnalyser findingsAnalyser = injector.getInstance(FindingsAnalyser.class);
        ResultReaderWriter resultReaderWriter = injector.getInstance(ResultReaderWriter.class);
        injector.getInstance(State.class).startPrintingTimer();

        String repoOwner = "TheAlgorithms";
        String repoName = "Java";

        Result result = findingsAnalyser.analyseFindings(repoOwner, repoName);

        resultReaderWriter.writeResult(result);
    }
}
