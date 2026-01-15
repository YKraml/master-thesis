import com.google.inject.Guice;
import com.google.inject.Injector;
import io.git.GitHubRepositorySelector;
import logic.StateHandler;
import util.GuiceModule;

import java.io.IOException;

public class MainCollectGitHubProjects {

    public static void main(String[] args) throws IOException, InterruptedException {
        Injector injector = Guice.createInjector(new GuiceModule());
        StateHandler stateHandler = injector.getInstance(StateHandler.class);
        GitHubRepositorySelector gitHubRepositorySelector = injector.getInstance(GitHubRepositorySelector.class);

        stateHandler.start();
        gitHubRepositorySelector.start(20000);
        stateHandler.end();
    }
}
