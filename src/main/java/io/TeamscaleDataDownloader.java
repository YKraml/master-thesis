package io;

import api.FindingChurnGetter;
import api.MetricsGetter;
import api.model.churn.FindingChurn;
import api.model.cleaned.Assessment;
import api.model.cleaned.CleanedMetrics;
import api.model.cleaned.TeamscaleData;
import api.model.cleaned.TeamscaleDataEntry;
import api.model.metrics.Metrics;
import api.model.metrics.ValueDouble;
import api.model.metrics.ValueMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import logic.State;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamscaleDataDownloader {

    private final ExecutorService executorService;
    private final State state;
    private final FindingChurnGetter findingChurnGetter;
    private final MetricsGetter metricsGetter;

    @Inject
    public TeamscaleDataDownloader(ExecutorService executorService, State state, FindingChurnGetter findingChurnGetter, MetricsGetter metricsGetter) {
        this.executorService = executorService;
        this.state = state;
        this.findingChurnGetter = findingChurnGetter;
        this.metricsGetter = metricsGetter;
    }

    public TeamscaleData downloadDataFrom(String repoOwner, String repoName) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        File file = new File("temp" + File.separator + "data" + File.separator + repoOwner + "." + repoName + ".json");
        if (file.exists()) {
            return new ObjectMapper().readValue(file, TeamscaleData.class);
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<TeamscaleDataEntry> data = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger counter = new AtomicInteger(0);
        List<Metrics> metricsList = metricsGetter.getMetrics(repoOwner, repoName, 1, Long.MAX_VALUE);
        for (Metrics metrics : metricsList) {
            futures.add(CompletableFuture.runAsync(() -> {
                state.addToState(Pair.of(repoOwner, repoName), "Downloading data from commit " + counter.getAndIncrement() + "/" + metricsList.size());
                FindingChurn findingChurn = getFindingChurnWithTimeout(repoOwner, repoName, metrics.getTimestamp(), 5);
                data.add(TeamscaleDataEntry.of(findingChurn, cleanMetrics(metrics)));
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(300, TimeUnit.SECONDS);
        data.sort((o1, o2) -> Math.toIntExact(o1.getMetrics().getTimestamp() / 1000 - o2.getMetrics().getTimestamp() / 1000));

        File folder = new File("temp" + File.separator + "data");
        if (!folder.exists()) {
            folder.mkdir();
        }

        file.createNewFile();
        TeamscaleData teamscaleData = TeamscaleData.of(repoOwner, repoName, data);
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, teamscaleData);
        return teamscaleData;
    }

    private static CleanedMetrics cleanMetrics(Metrics metrics) {
        int time = Math.toIntExact(metrics.getTimestamp() / 1000);
        int totalNumberOfFiles = (int) ((ValueDouble) metrics.getValue().get(0)).getValue();
        int totalNumberOfLinesOfCode = (int) ((ValueDouble) metrics.getValue().get(1)).getValue();
        int totalNumberOfSourceLinesOfCode = (int) ((ValueDouble) metrics.getValue().get(2)).getValue();
        ValueMapping fileSizeAssessment = (ValueMapping) metrics.getValue().get(3);
        int fileSizeAssessmentRed = (int) fileSizeAssessment.getRed();
        int fileSizeAssessmentYellow = (int) fileSizeAssessment.getYellow();
        int fileSizeAssessmentGreen = (int) fileSizeAssessment.getGreen();

        ValueMapping methodLengthAssessment = (ValueMapping) metrics.getValue().get(5);
        int methodLengthAssessmentRed = (int) methodLengthAssessment.getRed();
        int methodLengthAssessmentYellow = (int) methodLengthAssessment.getYellow();
        int methodLengthAssessmentGreen = (int) methodLengthAssessment.getGreen();

        ValueMapping nestingDepthAssessment = (ValueMapping) metrics.getValue().get(7);
        int nestingDepthAssessmentRed = (int) nestingDepthAssessment.getRed();
        int nestingDepthAssessmentYellow = (int) nestingDepthAssessment.getYellow();
        int nestingDepthAssessmentGreen = (int) nestingDepthAssessment.getGreen();

        double cloneCoverage = ((ValueDouble) metrics.getValue().get(17)).getValue();

        return CleanedMetrics.of(time,
                totalNumberOfFiles, totalNumberOfLinesOfCode, totalNumberOfSourceLinesOfCode,
                Assessment.of(fileSizeAssessmentRed, fileSizeAssessmentYellow, fileSizeAssessmentGreen),
                Assessment.of(methodLengthAssessmentRed, methodLengthAssessmentYellow, methodLengthAssessmentGreen),
                Assessment.of(nestingDepthAssessmentRed, nestingDepthAssessmentYellow, nestingDepthAssessmentGreen),
                cloneCoverage
        );
    }

    public FindingChurn getFindingChurnWithTimeout(String repoOwner, String repoName, long timestamp, int retries) {

        if (retries <= 0) {
            throw new RuntimeException("Could not get findings churn");
        }

        try {
            return CompletableFuture.supplyAsync(() ->
                    findingChurnGetter.getFindingsChurn(repoOwner, repoName, timestamp)).get(5, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            try {
                synchronized (this) {
                    Thread.sleep(1000);
                    return getFindingChurnWithTimeout(repoOwner, repoName, timestamp, retries - 1);
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
