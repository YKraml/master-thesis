package io.teamscale;

import com.google.inject.Inject;
import io.json.TeamscaleDataReaderWriter;
import io.teamscale.api.FindingChurnFetcher;
import io.teamscale.api.MetricsFetcher;
import io.teamscale.api.ProjectsFetcher;
import io.teamscale.api.model.churn.FindingChurn;
import io.teamscale.api.model.cleaned.CleanedMetrics;
import io.teamscale.api.model.cleaned.TeamscaleData;
import io.teamscale.api.model.cleaned.TeamscaleDataEntry;
import io.teamscale.api.model.metrics.Metrics;
import io.teamscale.api.model.metrics.ValueDouble;
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
            String[] split = project.name().split("\\.", 2);
            String repoOwner = split[0];
            String repoName = split[1];
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Start collecting data");
            try {
                if (teamscaleDataReaderWriter.read(repoOwner, repoName).isEmpty()) {
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
            //futures.add(CompletableFuture.runAsync(() -> {
            FindingChurn findingChurn = getFindingChurnWithTimeout(repoOwner, repoName, entry.timestamp(), 5);
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Downloaded data from commit " + counter.getAndIncrement() + "/" + teamscaleCommitCount);
            data.add(new TeamscaleDataEntry(findingChurn, cleanMetrics(entry)));
            //}, executorService));
        }

        // Wait here until all data is downloaded
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get();

        //Sort data by timestamp
        data.sort(Comparator.comparing(o -> o.findingChurn().commit().timestamp()));

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

        return new CleanedMetrics(
                Math.toIntExact(metrics.timestamp() / 1000),
                (int) ((ValueDouble) metrics.value().get(0)).value(),
                (int) ((ValueDouble) metrics.value().get(1)).value(),
                (int) ((ValueDouble) metrics.value().get(2)).value(),
                ((ValueDouble) metrics.value().get(11)).value(),
                ((ValueDouble) metrics.value().get(17)).value()
        );
    }
}
