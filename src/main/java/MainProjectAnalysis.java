import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.AnalysisStarter;
import logic.State;
import util.GuiceModule;

import java.io.IOException;

public class MainProjectAnalysis {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(State.class).startPrintingTimer();
        injector.getInstance(AnalysisStarter.class).startAnalysis();
    }
}
