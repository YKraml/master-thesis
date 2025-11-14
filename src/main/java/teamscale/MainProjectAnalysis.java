package teamscale;

import com.google.inject.Guice;
import com.google.inject.Injector;
import common.GuiceModule;
import common.ResultReaderWriter;
import linter.searching.logic.State;
import org.eclipse.jgit.api.errors.GitAPIException;
import teamscale.api.ProjectsGetter;
import teamscale.api.model.project.Project;
import teamscale.logic.AnalysisStarter;
import teamscale.logic.FindingsAnalyser;
import teamscale.logic.ResultNormaliser;
import teamscale.model.Result;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

public class MainProjectAnalysis {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(State.class).startPrintingTimer();
        injector.getInstance(AnalysisStarter.class).startAnalysis();
    }
}
