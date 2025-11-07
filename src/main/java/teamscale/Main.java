package teamscale;

import com.google.inject.Guice;
import com.google.inject.Injector;
import common.GuiceModule;
import linter.searching.logic.State;
import org.eclipse.jgit.api.errors.GitAPIException;
import teamscale.model.churn.Result;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, GitAPIException {
        Injector injector = Guice.createInjector(new GuiceModule());
        //injector.getInstance(ProjectCreator.class).createAllProjects();

        FindingsAnalyser findingsAnalyser = injector.getInstance(FindingsAnalyser.class);
        injector.getInstance(State.class).startPrintingTimer();

        String repoOwner = "airbnb";
        String repoName = "DeepLinkDispatch";

        Result result = findingsAnalyser.analyseFindings(repoOwner, repoName);
        for (Result.Entry entry : result.getEntries()) {
            System.out.println(entry);
        }

        System.out.println(result);

    }

}
