package teamscale;

import com.google.inject.Guice;
import com.google.inject.Injector;
import common.GuiceModule;
import linter.searching.logic.State;
import org.eclipse.jgit.api.errors.GitAPIException;
import teamscale.logic.AnalysisStarter;
import teamscale.logic.ResultNormaliser;

import java.io.IOException;

public class MainResultNormalisation {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(State.class).startPrintingTimer();
        injector.getInstance(ResultNormaliser.class).normaliseResults();
    }
}
