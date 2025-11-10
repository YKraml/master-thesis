package teamscale.logic;

import jakarta.inject.Inject;
import linter.searching.logic.RepositoryDownloader;
import linter.searching.logic.State;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import teamscale.api.FindingsChurnGetter;
import teamscale.api.MetricsGetter;
import teamscale.api.model.churn.FindingsChurn;
import teamscale.api.model.metrics.Metrics;
import teamscale.model.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class FindingsAnalyser {

    private final ExecutorService executorService;
    private final RepositoryDownloader repositoryDownloader;
    private final State state;
    private final FindingsChurnGetter findingsChurnGetter;
    private final MetricsGetter metricsGetter;

    @Inject
    public FindingsAnalyser(ExecutorService executorService, RepositoryDownloader repositoryDownloader, State state, FindingsChurnGetter findingsChurnGetter, MetricsGetter metricsGetter) {
        this.executorService = executorService;
        this.repositoryDownloader = repositoryDownloader;
        this.state = state;
        this.findingsChurnGetter = findingsChurnGetter;
        this.metricsGetter = metricsGetter;
    }

    public Result analyseFindings(String repoOwner, String repoName) throws IOException, GitAPIException {
        Pair<String, String> key = Pair.of(repoOwner, repoName);
        state.addToState(key, "Analysing findings");
        Git repo = repositoryDownloader.getRepo(repoOwner, repoName);
        List<RevCommit> commits = new ArrayList<>();
        repo.log().call().iterator().forEachRemaining(commits::add);
        commits = commits.reversed();

        List<Pair<FindingsChurn, Metrics>> data = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<?>> futures = new ArrayList<>();
        AtomicInteger current = new AtomicInteger();
        for (RevCommit commit : commits) {
            List<RevCommit> finalCommits = commits;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                state.addToState(key, "Analysing finding from commit " + current.getAndIncrement() + "/" + finalCommits.size());
                long time = commit.getCommitTime() * 1000L;
                FindingsChurn churn = findingsChurnGetter.getFindingsChurn(repoOwner, repoName, time);
                Metrics metrics = metricsGetter.getMetrics(repoOwner, repoName, time);

                data.add(Pair.of(churn, metrics));
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        //Sort data by date
        data.sort((o1, o2) -> Math.toIntExact(o1.getLeft().getCommit().getTimestamp() / 1000 - o2.getLeft().getCommit().getTimestamp() / 1000));

        state.removeFromState(key);

        return new Result(repoOwner, repoName, data);
    }
}
