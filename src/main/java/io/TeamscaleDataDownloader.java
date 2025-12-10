package io;

import api.CommitChartGetter;
import api.FindingChurnGetter;
import api.MetricsGetter;
import api.model.churn.Commit;
import api.model.churn.FindingChurn;
import api.model.cleaned.Assessment;
import api.model.cleaned.CleanedMetrics;
import api.model.cleaned.TeamscaleData;
import api.model.cleaned.TeamscaleDataEntry;
import api.model.commit.chart.Commits;
import api.model.commit.chart.UserWithCommits;
import api.model.metrics.Metrics;
import api.model.metrics.ValueDouble;
import api.model.metrics.ValueMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import logic.RepositoryHandler;
import logic.State;
import model.Proof;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TeamscaleDataDownloader {

    private final ExecutorService executorService;
    private final State state;
    private final FindingChurnGetter findingChurnGetter;
    private final MetricsGetter metricsGetter;
    private final RepositoryDownloader repositoryDownloader;
    private final RepositoryHandler repositoryHandler;
    private final CommitChartGetter commitChartGetter;

    @Inject
    public TeamscaleDataDownloader(ExecutorService executorService, State state, FindingChurnGetter findingChurnGetter, MetricsGetter metricsGetter, RepositoryDownloader repositoryDownloader, RepositoryHandler repositoryHandler, CommitChartGetter commitChartGetter) {
        this.executorService = executorService;
        this.state = state;
        this.findingChurnGetter = findingChurnGetter;
        this.metricsGetter = metricsGetter;
        this.repositoryDownloader = repositoryDownloader;
        this.repositoryHandler = repositoryHandler;
        this.commitChartGetter = commitChartGetter;
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

        return new CleanedMetrics(time,
                totalNumberOfFiles, totalNumberOfLinesOfCode, totalNumberOfSourceLinesOfCode,
                Assessment.of(fileSizeAssessmentRed, fileSizeAssessmentYellow, fileSizeAssessmentGreen),
                Assessment.of(methodLengthAssessmentRed, methodLengthAssessmentYellow, methodLengthAssessmentGreen),
                Assessment.of(nestingDepthAssessmentRed, nestingDepthAssessmentYellow, nestingDepthAssessmentGreen),
                cloneCoverage
        );
    }

    public TeamscaleData downloadDataFrom(String repoOwner, String repoName) throws IOException, ExecutionException, InterruptedException, TimeoutException, GitAPIException {
        File file = new File("temp" + File.separator + "data" + File.separator + repoOwner + "." + repoName + ".json");
        if (file.exists()) {
            //return new ObjectMapper().readValue(file, TeamscaleData.class);
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<TeamscaleDataEntry> data = Collections.synchronizedList(new ArrayList<>());

        // Get metrics data
        List<Metrics> metrics = metricsGetter.getMetrics(repoOwner, repoName, 1, Long.MAX_VALUE);

        // Get findings data
        AtomicInteger counter = new AtomicInteger(0);
        int teamscaleCommitCount = metrics.size();
        for (Metrics entry : metrics) {
            futures.add(CompletableFuture.runAsync(() -> {
                state.addToState(Pair.of(repoOwner, repoName), "Downloading data from commit " + counter.getAndIncrement() + "/" + teamscaleCommitCount);
                FindingChurn findingChurn = getFindingChurnWithTimeout(repoOwner, repoName, entry.getTimestamp(), 5);
                data.add(TeamscaleDataEntry.of(findingChurn, cleanMetrics(entry)));
            }, executorService));
        }

        // Wait here until all data is downloaded
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(300, TimeUnit.SECONDS);

        // Add data for missing commits
        List<RevCommit> repoCommits = getCommits(repoOwner, repoName);
        Set<String> teamscaleCommits = commitChartGetter.getCommitChart(repoOwner, repoName)
                .getCommits()
                .stream()
                .flatMap((Function<UserWithCommits, Stream<Commits>>) userWithCommits -> userWithCommits.getCommits().stream())
                .sorted(Comparator.comparing(o -> o.getCommit().getTimestamp()))
                .map(Commits::getRevision)
                .collect(Collectors.toSet());

        for (RevCommit commit : repoCommits) {

            if (teamscaleCommits.contains(commit.getName())) {
                continue;
            }

            data.add(TeamscaleDataEntry.of(
                    new FindingChurn(
                            new Commit("INSERTED", "INSERTED", ((long) commit.getCommitTime()) * 1000),
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    ),
                    cleanMetrics(getMetricsAtTime(metrics, commit.getCommitTime()))
            ));
        }

        //Sort data by timestamp
        data.sort(Comparator.comparing(o -> o.getFindingChurn().getCommit().getTimestamp()));

        File folder = new File("temp" + File.separator + "data");
        if (!folder.exists()) {
            folder.mkdir();
        }

        file.createNewFile();
        TeamscaleData teamscaleData = TeamscaleData.of(repoOwner, repoName, data);
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, teamscaleData);
        return teamscaleData;
    }

    private Metrics getMetricsAtTime(List<Metrics> metrics, int commitTime) {

        for (int i = 1; i < metrics.size(); i++) {
            Metrics metric = metrics.get(i);
            int metricTime = Math.toIntExact(metric.getTimestamp() / 1000);

            if (metricTime > commitTime) {
                return metrics.get(i - 1);
            }
        }

        return metrics.getLast();
    }

    private List<RevCommit> getCommits(String repoOwner, String repoName) throws GitAPIException, IOException {
        List<RevCommit> commits = new ArrayList<>();
        repositoryDownloader.getRepo(repoOwner, repoName).log().call().forEach(commits::add);
        commits = commits.reversed();

        Proof proof = repositoryHandler.handleRepo(repoOwner, repoName);
        commits = commits.stream().filter(revCommit -> revCommit.getCommitTime() >= proof.analysisStartTime() && revCommit.getCommitTime() <= proof.analysisEndTime()).toList();
        return commits;
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
