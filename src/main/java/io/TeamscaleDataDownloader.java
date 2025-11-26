package io;

import api.FindingChurnGetter;
import api.MetricsGetter;
import api.ProjectsGetter;
import api.model.churn.FindingChurn;
import api.model.cleaned.Assessment;
import api.model.cleaned.CleanedMetrics;
import api.model.cleaned.TeamscaleData;
import api.model.cleaned.TeamscaleDataEntry;
import api.model.metrics.Metrics;
import api.model.metrics.ValueDouble;
import api.model.metrics.ValueMapping;
import api.model.project.Project;
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
import java.util.function.Supplier;

public class TeamscaleDataDownloader {

    private final ProjectsGetter projectsGetter;

    private final ExecutorService executorService;
    private final State state;
    private final FindingChurnGetter findingChurnGetter;
    private final MetricsGetter metricsGetter;

    @Inject
    public TeamscaleDataDownloader(ProjectsGetter projectsGetter, ExecutorService executorService, State state, FindingChurnGetter findingChurnGetter, MetricsGetter metricsGetter) {
        this.projectsGetter = projectsGetter;
        this.executorService = executorService;
        this.state = state;
        this.findingChurnGetter = findingChurnGetter;
        this.metricsGetter = metricsGetter;
    }

    public void startDownload() {
        List<Project> existingProjects = projectsGetter.getExistingProjects();
        for (Project existingProject : existingProjects) {
            try {
                String[] split = existingProject.getName().split("\\.", 2);
                String repoOwner = split[0];
                String repoName = split[1];

                state.addToState(Pair.of(repoOwner, repoName), "Downloading data from Teamscale");
                downloadDataFrom(repoOwner, repoName);
                state.removeFromState(Pair.of(repoOwner, repoName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public TeamscaleData downloadDataFrom(String repoOwner, String repoName) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        File file = new File("temp" + File.separator + "data" + File.separator + repoOwner + "." + repoName + ".json");
        if (file.exists()) {
            return new ObjectMapper().readValue(file, TeamscaleData.class);
        }

        List<CompletableFuture<?>> futures = Collections.synchronizedList(new ArrayList<>());
        List<TeamscaleDataEntry> data = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger counter = new AtomicInteger(0);
        for (Metrics metrics : metricsGetter.getMetrics(repoOwner, repoName, 1, Long.MAX_VALUE)) {
            futures.add(CompletableFuture.runAsync(() -> {
                state.addToState(Pair.of(repoOwner, repoName), "Downloading data from commit " + counter.getAndIncrement() + "/" + metricsGetter.getMetrics(repoOwner, repoName, 1, Long.MAX_VALUE).size());
                FindingChurn findingChurn = getFindingChurnWithTimeout(repoOwner, repoName, metrics.getTimestamp(), 5);
                data.add(TeamscaleDataEntry.of(findingChurn, cleanMetrics(metrics)));
            }, executorService));
        }


        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(300, TimeUnit.SECONDS);
        data.sort((o1, o2) -> Math.toIntExact(o1.getMetrics().getTimestamp() / 1000 - o2.getMetrics().getTimestamp() / 1000));

        File folder = new File("data");
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

    public FindingChurn getFindingChurnWithTimeout(String repoOwner, String repoName, long timestamp, int retires) {
        int timeout = 5;

        Supplier<FindingChurn> supplier = () ->
                findingChurnGetter.getFindingsChurn(repoOwner, repoName, timestamp);

        if (retires == 0) {
            throw new RuntimeException("Coul not get findings churn");
        }

        try {
            return CompletableFuture.supplyAsync(supplier).get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            return getFindingChurnWithTimeout(repoOwner, repoName, timestamp, --retires);
        }
    }

}
