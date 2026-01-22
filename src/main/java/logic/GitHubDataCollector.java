package logic;

import io.git.graphql.ForkFetcher;
import io.git.graphql.IssueFetcher;
import io.git.graphql.model.forks.Fork;
import io.git.graphql.model.issues.Issue;
import io.json.CemProjectPairReaderWriter;
import io.json.GitHubDataReaderWriter;
import jakarta.inject.Inject;
import model.CemProject;
import model.GitHubData;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class GitHubDataCollector {

    private final GitHubDataReaderWriter gitHubDataReaderWriter;
    private final CemProjectPairReaderWriter cemProjectPairReaderWriter;

    private final StateHandler stateHandler;

    private final ForkFetcher forkFetcher;
    private final IssueFetcher issueFetcher;

    @Inject
    public GitHubDataCollector(GitHubDataReaderWriter gitHubDataReaderWriter, CemProjectPairReaderWriter cemProjectPairReaderWriter, StateHandler stateHandler, ForkFetcher forkFetcher, IssueFetcher issueFetcher) {
        this.gitHubDataReaderWriter = gitHubDataReaderWriter;
        this.cemProjectPairReaderWriter = cemProjectPairReaderWriter;
        this.stateHandler = stateHandler;
        this.forkFetcher = forkFetcher;
        this.issueFetcher = issueFetcher;
    }

    public void startCollecting() throws IOException {
        System.out.println("Reading cem projects");
        List<CemProject> cemProjects = cemProjectPairReaderWriter
                .readAll()
                .stream()
                .flatMap(cemProjectPair -> Stream.of(cemProjectPair.treated(), cemProjectPair.control()))
                .toList();

        for (CemProject cemProject : cemProjects) {
            String repoOwner = cemProject.repoOwner();
            String repoName = cemProject.repoName();

            stateHandler.addToState(Pair.of(repoOwner, repoName), "Collecting data from GitHub");

            if (gitHubDataReaderWriter.getElement(repoOwner, repoName).isEmpty()) {
                List<Issue> issues = issueFetcher.fetch(repoOwner, repoName);
                List<Fork> forks = forkFetcher.fetch(repoOwner, repoName);
                gitHubDataReaderWriter.write(new GitHubData(repoOwner, repoName, forks, issues));
            }

            stateHandler.removeFromState(Pair.of(repoOwner, repoName));
        }

        System.out.println();
    }

}
