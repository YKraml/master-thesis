import api.model.cleaned.TeamscaleData;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.TeamscaleDataDownloader;
import logic.State;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import util.GuiceModule;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class MainTest {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, TimeoutException, GitAPIException {
        Injector injector = Guice.createInjector(new GuiceModule());
        State state = injector.getInstance(State.class);
        TeamscaleDataDownloader teamscaleDataDownloader = injector.getInstance(TeamscaleDataDownloader.class);



        state.start();

        String repoOwner = "gephi";
        String repoName = "gephi";
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom(repoOwner, repoName);
        state.removeFromState(Pair.of(repoOwner, repoName));

        System.out.println(teamscaleData);

        state.end();
    }
}
