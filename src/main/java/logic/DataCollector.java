package logic;

import com.google.inject.Inject;
import io.json.CemProjectPairReaderWriter;
import io.json.CombinedDataReaderWriter;
import io.json.ProofReaderWriter;
import io.teamscale.TeamscaleDataDownloader;
import io.teamscale.api.ProjectsFetcher;
import io.teamscale.api.model.cleaned.TeamscaleData;
import io.teamscale.api.model.project.Project;
import model.CemProject;
import model.CombinedData;
import model.Proof;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class DataCollector {

    private final StateHandler stateHandler;

    private final ProofReaderWriter proofReaderWriter;
    private final CombinedDataReaderWriter combinedDataReaderWriter;
    private final CemProjectPairReaderWriter projectPairReaderWriter;

    private final TeamscaleDataDownloader teamscaleDataDownloader;
    private final ProjectsFetcher projectsFetcher;
    private final DataCombiner dataCombiner;

    @Inject
    public DataCollector(ProjectsFetcher projectsFetcher, StateHandler stateHandler, TeamscaleDataDownloader teamscaleDataDownloader, DataCombiner dataCombiner, CombinedDataReaderWriter combinedDataReaderWriter, ProofReaderWriter proofReaderWriter, CemProjectPairReaderWriter projectPairReaderWriter) {
        this.projectsFetcher = projectsFetcher;
        this.stateHandler = stateHandler;
        this.teamscaleDataDownloader = teamscaleDataDownloader;
        this.dataCombiner = dataCombiner;
        this.combinedDataReaderWriter = combinedDataReaderWriter;
        this.proofReaderWriter = proofReaderWriter;
        this.projectPairReaderWriter = projectPairReaderWriter;
    }

    public void startCollectingData() throws IOException {
        System.out.println("Fetching projects from teamscale");
        List<Project> existingProjects = projectsFetcher.getExistingProjects();

        List<CemProject> cemProjects = projectPairReaderWriter.readAll()
                .stream()
                .flatMap(cemProjectPair -> Stream.of(cemProjectPair.treated(), cemProjectPair.control()))
                .toList();

        // Collect data for each project
        for (Project project : existingProjects) {
            String[] split = project.getName().split("\\.", 2);
            String repoOwner = split[0];
            String repoName = split[1];
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Start collecting data");
            try {
                CombinedData combinedData = collectDataFor(repoOwner, repoName, cemProjects);
                combinedDataReaderWriter.write(combinedData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            stateHandler.removeFromState(Pair.of(repoOwner, repoName));
        }
    }

    private CombinedData collectDataFor(String repoOwner, String repoName, List<CemProject> cemProjects) throws Exception {
        stateHandler.addToState(Pair.of(repoOwner, repoName), "Start collecting data");
        Proof proof = proofReaderWriter.getElement(repoOwner, repoName).orElseThrow();
        CemProject cemProject = cemProjects.stream().filter(project -> project.repoOwner().equals(repoOwner) && project.repoName().equals(repoName)).findFirst().orElseThrow();
        TeamscaleData teamscaleData = teamscaleDataDownloader.downloadDataFrom(repoOwner, repoName);

        return dataCombiner.combineData(proof, teamscaleData, cemProject);
    }
}
