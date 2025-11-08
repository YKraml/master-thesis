package teamscale;

import com.google.inject.Guice;
import com.google.inject.Injector;
import common.GuiceModule;
import linter.searching.logic.State;
import org.eclipse.jgit.api.errors.GitAPIException;
import teamscale.model.churn.Result;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args) throws IOException, GitAPIException {
        Injector injector = Guice.createInjector(new GuiceModule());
        //injector.getInstance(ProjectCreator.class).createAllProjects();

        FindingsAnalyser findingsAnalyser = injector.getInstance(FindingsAnalyser.class);
        injector.getInstance(State.class).startPrintingTimer();

        String repoOwner = "TheAlgorithms";
        String repoName = "Java";

        Result result = findingsAnalyser.analyseFindings(repoOwner, repoName);
        writeResult(result);
    }

    public static void writeResult(Result result) throws IOException {
        File resultFile = getResultFile(result.getRepoOwner(), result.getRepoName());
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }

        try (PrintWriter pw = new PrintWriter(resultFile)) {
            for (Result.Entry entry : result.getEntries()) {
                String line = entry.time() + "," + entry.numberOfCodeClones() + "," + entry.numberOfFileSizeViolations() + "," + entry.numberOfMethodLengthViolations() + "," + entry.numberOfNestingDepthViolations();
                pw.println(line);
            }
        }


    }

    public static File getResultFile(String owner, String repoName) {
        return new File("results" + File.separator + owner + "." + repoName + ".csv");
    }

}
