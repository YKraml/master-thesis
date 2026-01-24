import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.GitHubRepoAnalyser;
import logic.StateHandler;
import util.GuiceModule;

import java.io.IOException;

public class MainAnalyseGitHubProjects {

    public static void main(String[] args) throws IOException, InterruptedException {
        Injector injector = Guice.createInjector(new GuiceModule());
        StateHandler stateHandler = injector.getInstance(StateHandler.class);
        GitHubRepoAnalyser gitHubRepoAnalyser = injector.getInstance(GitHubRepoAnalyser.class);

        stateHandler.start();
        gitHubRepoAnalyser.startAnalysing();
        stateHandler.end();
    }
}
