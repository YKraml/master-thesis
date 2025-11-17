import com.google.inject.Guice;
import com.google.inject.Injector;
import logic.FindingsAnalyser;
import logic.State;
import util.GuiceModule;

import java.io.IOException;

public class MainProjectAnalysis {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        State state = injector.getInstance(State.class);
        FindingsAnalyser findingsAnalyser = injector.getInstance(FindingsAnalyser.class);

        state.startPrintingTimer();
        findingsAnalyser.startAnalysis();
        //state.stopPrintingTimer();
    }
}
