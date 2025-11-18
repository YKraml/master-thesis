import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.DataAnalyser;
import logic.State;
import util.GuiceModule;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class MainAnalyseTeamscaleData {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        State state = injector.getInstance(State.class);
        ExecutorService executorService = injector.getInstance(ExecutorService.class);
        DataAnalyser dataAnalyser = injector.getInstance(DataAnalyser.class);

        state.start();
        dataAnalyser.startAnalysis();
        state.end();
    }
}
