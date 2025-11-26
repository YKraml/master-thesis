import api.ProjectsGetter;
import api.model.cleaned.TeamscaleData;
import api.model.project.Project;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.CsvReaderWriter;
import io.ProofReaderWriter;
import io.TeamscaleDataDownloader;
import logic.DataAnalyser;
import logic.RepositoryHandler;
import logic.State;
import model.Proof;
import model.Result;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import util.GuiceModule;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public class MainPreparePythonScript {

    private final ProjectsGetter projectsGetter;
    private final CsvReaderWriter csvReaderWriter;
    private final State state;
    private final RepositoryHandler repositoryHandler;
    private final TeamscaleDataDownloader teamscaleDataDownloader;
    private final ExecutorService executorService;
    private final DataAnalyser dataAnalyser;
    private final ProofReaderWriter proofReaderWriter;

    @Inject
    public MainPreparePythonScript(ProjectsGetter projectsGetter, CsvReaderWriter csvReaderWriter, State state, RepositoryHandler repositoryHandler, TeamscaleDataDownloader teamscaleDataDownloader, ExecutorService executorService, DataAnalyser dataAnalyser, ProofReaderWriter proofReaderWriter) {
        this.projectsGetter = projectsGetter;
        this.csvReaderWriter = csvReaderWriter;
        this.state = state;
        this.repositoryHandler = repositoryHandler;
        this.teamscaleDataDownloader = teamscaleDataDownloader;
        this.executorService = executorService;
        this.dataAnalyser = dataAnalyser;
        this.proofReaderWriter = proofReaderWriter;
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new GuiceModule());
        injector.getInstance(State.class).start();
        injector.getInstance(MainPreparePythonScript.class).prepareData();
        injector.getInstance(State.class).end();
    }

    private void prepareData() {
        List<Project> existingProjects = projectsGetter.getExistingProjects();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Project existingProject : existingProjects) {
            String[] split = existingProject.getName().split("\\.", 2);
            String repoOwner = split[0];
            String repoName = split[1];

            //futures.add(CompletableFuture.runAsync(() -> {
            state.addToState(Pair.of(repoOwner, repoName), "Prepare data for python");
            try {
                prepareData(repoOwner, repoName);
            } catch (GitAPIException | IOException | ExecutionException | InterruptedException |
                     TimeoutException e) {
                e.printStackTrace();
            }
            state.removeFromState(Pair.of(repoOwner, repoName));
            //}, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void prepareData(String repoOwner, String repoName) throws GitAPIException, IOException, ExecutionException, InterruptedException, TimeoutException {
        Proof proof = repositoryHandler.handleRepo(repoOwner, repoName).get();
        //Optional<Proof> proofOptional = proofReaderWriter.getProofOfRepo(repoOwner, repoName);
        //if (proofOptional.isEmpty()) {
        //    return;
        //}
        //Proof proof = proofOptional.get();

        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom(repoOwner, repoName);
        Result result = dataAnalyser.analyseTeamscaleData(teamscaleData);

        Integer timestamp = teamscaleData.getEntries().getFirst().getMetrics().getTimestamp();
        for (Proof.LinterWithProof linter : proof.linters()) {
            int introductionTime = linter.introductionTime();

            int secondsInTwoYears = 2 * 365 * 24 * 60 * 60;
            if (introductionTime - (secondsInTwoYears * 0.90) < timestamp) {
                return;
            }
        }

        writeResultAsCsv(result);
        writeProofAsCsv(proof);
    }

    private void writeProofAsCsv(Proof proof) throws IOException {
        String repoOwner = proof.repoOwner();
        String repoName = proof.repoName();
        File resultFile = new File("python" + File.separator + "root" + File.separator + "csv" + File.separator + repoOwner + "." + repoName + ".linter.csv");
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }

        try (PrintWriter pw = new PrintWriter(resultFile)) {
            pw.println("time,linter");
            for (Proof.LinterWithProof linter : proof.linters()) {
                pw.println(linter.introductionTime() + "," + linter.linterName());
            }
        }
    }

    public void writeResultAsCsv(Result result) throws IOException {
        String repoOwner = result.repoOwner();
        String repoName = result.repoName();
        File resultFile = new File("python" + File.separator + "root" + File.separator + "csv" + File.separator + repoOwner + "." + repoName + ".data.csv");
        if (!resultFile.getParentFile().exists()) {
            resultFile.getParentFile().mkdirs();
        }
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }

        try (PrintWriter pw = new PrintWriter(resultFile)) {
            pw.println(Result.Entry.csvHeader());
            for (Result.Entry entry : result.entries()) {
                pw.println(entry.csvLine());
            }
        }
    }
}
