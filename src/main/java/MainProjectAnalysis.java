import com.google.inject.Guice;
import com.google.inject.Injector;
import util.GuiceModule;
import logic.State;
import logic.AnalysisStarter;

public class MainProjectAnalysis {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(State.class).startPrintingTimer();
        injector.getInstance(AnalysisStarter.class).startAnalysis();
    }
}
