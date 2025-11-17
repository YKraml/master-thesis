import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.State;
import io.TeamscaleDataDownloader;
import util.GuiceModule;

import java.io.IOException;

public class MainTeamscaleDataDownload {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        State state = injector.getInstance(State.class);
        TeamscaleDataDownloader teamscaleDataDownloader = injector.getInstance(TeamscaleDataDownloader.class);

        state.startPrintingTimer();
        teamscaleDataDownloader.startDownload();
    }

}
