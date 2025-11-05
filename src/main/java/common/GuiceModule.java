package common;

import com.google.inject.Binder;
import com.google.inject.Module;
import linter.searching.logic.State;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuiceModule implements Module {
    @Override
    public void configure(Binder binder) {

        try {
            binder.bind(GitHub.class).toInstance(new GitHubBuilder().withOAuthToken(Constants.TOKEN).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        binder.bind(State.class).toInstance(new State());
        binder.bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSORS));
    }
}
