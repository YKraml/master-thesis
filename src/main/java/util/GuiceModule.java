package util;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import logic.StateHandler;
import org.kohsuke.github.GHRateLimit;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.concurrent.*;

public class GuiceModule implements Module {
    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton
    static StateHandler provideState(ExecutorService executorService) {
        return new StateHandler(executorService);
    }

    @Provides
    @Singleton
    static GitHub provideGitHub() throws IOException, ExecutionException, InterruptedException {
        try {
            GitHub gitHub = CompletableFuture.supplyAsync(() -> {
                try {
                    return new GitHubBuilder().withOAuthToken(Constants.GITHUB_TOKEN).build();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).get(10, TimeUnit.SECONDS);

            GHRateLimit rateLimit = gitHub.getRateLimit();
            String text = "GitHub rate limits:\n" +
                    "Core: " + rateLimit.getCore() + "\n" +
                    "GraphQL:" + rateLimit.getGraphQL() + "\n";
            System.out.println(text);

            return gitHub;
        } catch (TimeoutException e) {
            throw new RuntimeException("Could not get GitHub api connection. Timeout.");
        }
    }

    @Provides
    @Singleton
    static ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSORS);
    }

    @Provides
    @Singleton
    static HttpClient provideHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }
}
