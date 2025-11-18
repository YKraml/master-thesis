import com.google.inject.Guice;
import com.google.inject.Injector;
import io.TeamscaleDataDownloader;
import logic.State;
import util.GuiceModule;

public class MainDownloadTeamscaleData {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        State state = injector.getInstance(State.class);
        TeamscaleDataDownloader teamscaleDataDownloader = injector.getInstance(TeamscaleDataDownloader.class);

        state.start();
        teamscaleDataDownloader.startDownload();
    }

}
