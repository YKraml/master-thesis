import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.GitHubDataCollector;
import logic.StateHandler;
import util.GuiceModule;

import java.io.IOException;

public class MainCollectGitHubData {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());

        StateHandler stateHandler = injector.getInstance(StateHandler.class);
        GitHubDataCollector gitHubDataCollector = injector.getInstance(GitHubDataCollector.class);

        stateHandler.start();
        gitHubDataCollector.startCollecting();
        stateHandler.end();
    }
}
