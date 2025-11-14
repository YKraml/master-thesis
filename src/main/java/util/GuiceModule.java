package util;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import logic.State;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuiceModule implements Module {
    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton
    static State provideState() {
        return new State();
    }

    @Provides
    @Singleton
    static GitHub provideGitHub() throws IOException {
        return new GitHubBuilder().withOAuthToken(Constants.TOKEN).build();
    }

    @Provides
    @Singleton
    static ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSORS);
    }

}
