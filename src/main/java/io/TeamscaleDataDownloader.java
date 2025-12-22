package io;

import api.CommitChartFetcher;
import api.FindingChurnFetcher;
import api.MetricsFetcher;
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
import logic.GitRepositoryHandler;
import logic.StateHandler;
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
    private final StateHandler stateHandler;
    private final FindingChurnFetcher findingChurnFetcher;
    private final MetricsFetcher metricsFetcher;
    private final RepositoryDownloader repositoryDownloader;
    private final GitRepositoryHandler gitRepositoryHandler;
    private final CommitChartFetcher commitChartFetcher;

    @Inject
    public TeamscaleDataDownloader(ExecutorService executorService, StateHandler stateHandler, FindingChurnFetcher findingChurnFetcher, MetricsFetcher metricsFetcher, RepositoryDownloader repositoryDownloader, GitRepositoryHandler gitRepositoryHandler, CommitChartFetcher commitChartFetcher) {
        this.executorService = executorService;
        this.stateHandler = stateHandler;
        this.findingChurnFetcher = findingChurnFetcher;
        this.metricsFetcher = metricsFetcher;
        this.repositoryDownloader = repositoryDownloader;
        this.gitRepositoryHandler = gitRepositoryHandler;
        this.commitChartFetcher = commitChartFetcher;
    }

    private static CleanedMetrics cleanMetrics(Metrics metrics) {
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

    public TeamscaleData downloadDataFrom(String repoOwner, String repoName) throws IOException, ExecutionException, InterruptedException, TimeoutException, GitAPIException {
        File file = new File("temp" + File.separator + "data" + File.separator + repoOwner + "." + repoName + ".json");
        if (file.exists()) {
            return new ObjectMapper().readValue(file, TeamscaleData.class);
        }

        List<CompletableFuture<?>> futures = new ArrayList<>();
        List<TeamscaleDataEntry> data = Collections.synchronizedList(new ArrayList<>());

        // Get metrics data
        List<Metrics> metrics = metricsFetcher.getMetrics(repoOwner, repoName, 1, Long.MAX_VALUE);

        // Get findings data
        AtomicInteger counter = new AtomicInteger(1);
        int teamscaleCommitCount = metrics.size();
        for (Metrics entry : metrics) {
            futures.add(CompletableFuture.runAsync(() -> {
                stateHandler.addToState(Pair.of(repoOwner, repoName), "Downloading data from commit " + counter.getAndIncrement() + "/" + teamscaleCommitCount);
                FindingChurn findingChurn = getFindingChurnWithTimeout(repoOwner, repoName, entry.getTimestamp(), 5);
                data.add(TeamscaleDataEntry.of(findingChurn, cleanMetrics(entry)));
            }, executorService));
        }

        // Wait here until all data is downloaded
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get(300, TimeUnit.SECONDS);

        // Add data for missing commits
        // addMissingData(repoOwner, repoName, data, metrics);

        //Sort data by timestamp
        data.sort(Comparator.comparing(o -> o.getFindingChurn().commit().timestamp()));

        File folder = new File("temp" + File.separator + "data");
        if (!folder.exists()) {
            folder.mkdir();
        }

        file.createNewFile();
        TeamscaleData teamscaleData = TeamscaleData.of(repoOwner, repoName, data);
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(file, teamscaleData);
        return teamscaleData;
    }

    private void addMissingData(String repoOwner, String repoName, List<TeamscaleDataEntry> data, List<Metrics> metrics) throws GitAPIException, IOException, InterruptedException {
        List<RevCommit> repoCommits = getCommits(repoOwner, repoName);
        Set<String> teamscaleCommits = commitChartFetcher.getCommitChart(repoOwner, repoName)
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
                    FindingChurn.empty(commit.getCommitTime()),
                    cleanMetrics(getMetricsAtTime(metrics, commit.getCommitTime()))
            ));
        }
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

        Proof proof = gitRepositoryHandler.handleRepo(repoOwner, repoName);
        commits = commits.stream().filter(revCommit -> revCommit.getCommitTime() >= proof.analysisStartTime() && revCommit.getCommitTime() <= proof.analysisEndTime()).toList();
        return commits;
    }

    public FindingChurn getFindingChurnWithTimeout(String repoOwner, String repoName, long timestamp, int retries) {

        if (retries <= 0) {
            throw new RuntimeException("Could not get findings churn");
        }

        try {
            return CompletableFuture.supplyAsync(() ->
                    findingChurnFetcher.getFindingsChurn(repoOwner, repoName, timestamp)).get(5, TimeUnit.SECONDS);
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
