import com.google.inject.Guice;
import com.google.inject.Injector;
import util.GuiceModule;
import logic.State;
import logic.ResultNormaliser;

import java.io.IOException;

public class MainResultNormalisation {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(State.class).startPrintingTimer();
        injector.getInstance(ResultNormaliser.class).normaliseResults();
    }
}
