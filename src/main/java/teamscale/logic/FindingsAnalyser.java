package teamscale.logic;

import common.Proof;
import jakarta.inject.Inject;
import linter.searching.logic.RepositoryDownloader;
import linter.searching.logic.RepositoryHandler;
import linter.searching.logic.State;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import teamscale.api.ChurnGetter;
import teamscale.api.MetricsGetter;
import teamscale.api.model.churn.Churn;
import teamscale.api.model.metrics.Metrics;
import teamscale.model.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FindingsAnalyser {

    private final RepositoryDownloader repositoryDownloader;
    private final State state;
    private final ChurnGetter churnGetter;
    private final MetricsGetter metricsGetter;
    private final RepositoryHandler repositoryHandler;

    @Inject
    public FindingsAnalyser(RepositoryDownloader repositoryDownloader, State state, ChurnGetter churnGetter, MetricsGetter metricsGetter, RepositoryHandler repositoryHandler) {
        this.repositoryDownloader = repositoryDownloader;
        this.state = state;
        this.churnGetter = churnGetter;
        this.metricsGetter = metricsGetter;
        this.repositoryHandler = repositoryHandler;
    }

    public Result analyseFindings(String repoOwner, String repoName) throws IOException, GitAPIException {
        Pair<String, String> key = Pair.of(repoOwner, repoName);
        state.addToState(key, "Analysing findings");

        Proof proof = repositoryHandler.handleRepo(repoOwner, repoName).get();

        long start = 1;
        long end = Long.MAX_VALUE;
        List<Metrics> metrics = metricsGetter.getMetrics(repoOwner, repoName, start, end);
        Churn churn = churnGetter.getFindingsChurn(repoOwner, repoName, start, end);

        state.removeFromState(key);

        return new Result(repoOwner, repoName, null);
    }
}
