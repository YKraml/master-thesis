package io.teamscale;

import com.google.inject.Inject;
import io.json.TeamscaleDataReaderWriter;
import io.teamscale.api.FindingChurnFetcher;
import io.teamscale.api.MetricsFetcher;
import io.teamscale.api.ProjectsFetcher;
import io.teamscale.api.model.churn.FindingChurn;
import io.teamscale.api.model.cleaned.Assessment;
import io.teamscale.api.model.cleaned.CleanedMetrics;
import io.teamscale.api.model.cleaned.TeamscaleData;
import io.teamscale.api.model.cleaned.TeamscaleDataEntry;
import io.teamscale.api.model.metrics.Metrics;
import io.teamscale.api.model.metrics.ValueDouble;
import io.teamscale.api.model.metrics.ValueMapping;
import io.teamscale.api.model.project.Project;
import logic.StateHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamscaleDataCollector {

    private final StateHandler stateHandler;
    private final ExecutorService executorService;

    private final TeamscaleDataReaderWriter teamscaleDataReaderWriter;

    private final ProjectsFetcher projectsFetcher;
    private final MetricsFetcher metricsFetcher;
    private final FindingChurnFetcher findingChurnFetcher;

    @Inject
    public TeamscaleDataCollector(ProjectsFetcher projectsFetcher, StateHandler stateHandler, ExecutorService executorService, TeamscaleDataReaderWriter teamscaleDataReaderWriter, MetricsFetcher metricsFetcher, FindingChurnFetcher findingChurnFetcher) {
        this.projectsFetcher = projectsFetcher;
        this.stateHandler = stateHandler;
        this.executorService = executorService;
        this.teamscaleDataReaderWriter = teamscaleDataReaderWriter;
        this.metricsFetcher = metricsFetcher;
        this.findingChurnFetcher = findingChurnFetcher;
    }

    public void startCollecting() {
        System.out.println("Fetching projects from teamscale");
        List<Project> existingProjects = projectsFetcher.getExistingProjects();

        // Collect teamscale data for each project
        for (Project project : existingProjects) {
            String[] split = project.getName().split("\\.", 2);
            String repoOwner = split[0];
            String repoName = split[1];
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Start collecting data");
            try {
                if (teamscaleDataReaderWriter.getElement(repoOwner, repoName).isEmpty()) {
                    teamscaleDataReaderWriter.write(downloadDataFrom(repoOwner, repoName));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stateHandler.removeFromState(Pair.of(repoOwner, repoName));
            }
        }
    }

    public TeamscaleData downloadDataFrom(String repoOwner, String repoName) throws ExecutionException, InterruptedException {

        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<TeamscaleDataEntry> data = Collections.synchronizedList(new ArrayList<>());

        // Get metrics data
        List<Metrics> metrics = metricsFetcher.getMetrics(repoOwner, repoName, 1, Long.MAX_VALUE);

        // Get findings data
        AtomicInteger counter = new AtomicInteger(1);
        int teamscaleCommitCount = metrics.size();
        for (Metrics entry : metrics) {
            futures.add(CompletableFuture.runAsync(() -> {
                FindingChurn findingChurn = getFindingChurnWithTimeout(repoOwner, repoName, entry.getTimestamp(), 5);
                stateHandler.addToState(Pair.of(repoOwner, repoName), "Downloaded data from commit " + counter.getAndIncrement() + "/" + teamscaleCommitCount);
                data.add(TeamscaleDataEntry.of(findingChurn, cleanMetrics(entry)));
            }, executorService));
        }

        // Wait here until all data is downloaded
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get();

        //Sort data by timestamp
        data.sort(Comparator.comparing(o -> o.getFindingChurn().commit().timestamp()));

        return new TeamscaleData(repoOwner, repoName, data);
    }

    public FindingChurn getFindingChurnWithTimeout(String repoOwner, String repoName, long timestamp, int retries) {
        if (retries <= 0) {
            throw new RuntimeException("Could not get findings churn. Retries exceeded.");
        }

        try {
            return CompletableFuture.supplyAsync(() ->
                    findingChurnFetcher.getFindingsChurn(repoOwner, repoName, timestamp)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            try {
                synchronized (this) {
                    Thread.sleep(10000);
                    return getFindingChurnWithTimeout(repoOwner, repoName, timestamp, retries - 1);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private CleanedMetrics cleanMetrics(Metrics metrics) {
        ValueMapping fileSizeAssessment = (ValueMapping) metrics.getValue().get(3);
        ValueMapping methodLengthAssessment = (ValueMapping) metrics.getValue().get(5);
        ValueMapping nestingDepthAssessment = (ValueMapping) metrics.getValue().get(7);
        ValueMapping commentCompletenessAssessment = (ValueMapping) metrics.getValue().get(18);

        return new CleanedMetrics(
                Math.toIntExact(metrics.getTimestamp() / 1000),
                (int) ((ValueDouble) metrics.getValue().get(0)).getValue(),
                (int) ((ValueDouble) metrics.getValue().get(1)).getValue(),
                (int) ((ValueDouble) metrics.getValue().get(2)).getValue(),
                Assessment.of((int) fileSizeAssessment.getRed(), (int) fileSizeAssessment.getYellow(), (int) fileSizeAssessment.getGreen()),
                (int) ((ValueDouble) metrics.getValue().get(4)).getValue(),
                Assessment.of((int) methodLengthAssessment.getRed(), (int) methodLengthAssessment.getYellow(), (int) methodLengthAssessment.getGreen()),
                (int) ((ValueDouble) metrics.getValue().get(6)).getValue(),
                Assessment.of((int) nestingDepthAssessment.getRed(), (int) nestingDepthAssessment.getYellow(), (int) nestingDepthAssessment.getGreen()),
                ((ValueDouble) metrics.getValue().get(10)).getValue(),
                ((ValueDouble) metrics.getValue().get(11)).getValue(),
                ((ValueDouble) metrics.getValue().get(12)).getValue(),
                ((ValueDouble) metrics.getValue().get(13)).getValue(),
                ((ValueDouble) metrics.getValue().get(14)).getValue(),
                ((ValueDouble) metrics.getValue().get(15)).getValue(),
                ((ValueDouble) metrics.getValue().get(16)).getValue(),
                ((ValueDouble) metrics.getValue().get(17)).getValue(),
                Assessment.of((int) commentCompletenessAssessment.getRed(), (int) commentCompletenessAssessment.getYellow(), (int) commentCompletenessAssessment.getGreen()),
                ((ValueDouble) metrics.getValue().get(19)).getValue(),
                ((ValueDouble) metrics.getValue().get(21)).getValue(),
                ((ValueDouble) metrics.getValue().get(22)).getValue(),
                ((ValueDouble) metrics.getValue().get(23)).getValue(),
                ((ValueDouble) metrics.getValue().get(24)).getValue(),
                ((ValueDouble) metrics.getValue().get(25)).getValue(),
                ((ValueDouble) metrics.getValue().get(26)).getValue(),
                ((ValueDouble) metrics.getValue().get(27)).getValue(),
                ((ValueDouble) metrics.getValue().get(28)).getValue(),
                ((ValueDouble) metrics.getValue().get(29)).getValue(),
                ((ValueDouble) metrics.getValue().get(30)).getValue()
        );
    }
}
