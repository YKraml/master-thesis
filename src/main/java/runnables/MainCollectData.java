package runnables;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.teamscale.TeamscaleDataCollector;
import logic.StateHandler;
import util.GuiceModule;

import java.io.IOException;

public class MainCollectData {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        StateHandler stateHandler = injector.getInstance(StateHandler.class);
        TeamscaleDataCollector teamscaleDataCollector = injector.getInstance(TeamscaleDataCollector.class);

        stateHandler.start();
        teamscaleDataCollector.startCollecting();
        stateHandler.end();
    }
}
