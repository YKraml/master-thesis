package runnables;

import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.GitHubRepoSelector;
import logic.StateHandler;
import util.Constants;
import util.GuiceModule;

import java.io.IOException;

public class MainCollectGitHubProjects {

    public static void main(String[] args) throws IOException, InterruptedException {
        Injector injector = Guice.createInjector(new GuiceModule());
        StateHandler stateHandler = injector.getInstance(StateHandler.class);
        GitHubRepoSelector gitHubRepoSelector = injector.getInstance(GitHubRepoSelector.class);

        stateHandler.start();
        gitHubRepoSelector.start(Constants.NUMBER_OF_REPOS);
        stateHandler.end();
    }
}
