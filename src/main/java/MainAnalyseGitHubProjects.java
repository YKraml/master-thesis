import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.GitHubRepoAnalyserStarter;
import logic.StateHandler;
import util.GuiceModule;

import java.io.IOException;

public class MainAnalyseGitHubProjects {

    public static void main(String[] args) throws IOException, InterruptedException {
        Injector injector = Guice.createInjector(new GuiceModule());
        StateHandler stateHandler = injector.getInstance(StateHandler.class);
        GitHubRepoAnalyserStarter gitHubRepoAnalyserStarter = injector.getInstance(GitHubRepoAnalyserStarter.class);

        stateHandler.start();
        gitHubRepoAnalyserStarter.start();
        stateHandler.end();
    }
}
