package teamscale;

import jakarta.inject.Inject;
import linter.searching.logic.RepositoryDownloader;
import linter.searching.logic.State;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import teamscale.model.churn.FindingsChurn;
import teamscale.model.churn.Result;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class FindingsAnalyser {

    private final ExecutorService executorService;
    private final RepositoryDownloader repositoryDownloader;
    private final State state;
    private final FindingsChurnGetter findingsChurnGetter;

    @Inject
    public FindingsAnalyser(ExecutorService executorService, RepositoryDownloader repositoryDownloader, State state, FindingsChurnGetter findingsChurnGetter) {
        this.executorService = executorService;
        this.repositoryDownloader = repositoryDownloader;
        this.state = state;
        this.findingsChurnGetter = findingsChurnGetter;
    }

    public Result analyseFindings(String repoOwner, String repoName) throws IOException, GitAPIException {
        Pair<String, String> key = Pair.of(repoOwner, repoName);
        state.addToState(key, "Analysing findings");
        Git repo = repositoryDownloader.getRepo(repoOwner, repoName);
        List<RevCommit> commits = new ArrayList<>();
        repo.log().call().iterator().forEachRemaining(commits::add);
        commits = commits.reversed();

        Set<Long> alreadyAnalyzed = new HashSet<>();
        List<FindingsChurn> churns = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<?>> futures = new ArrayList<>();
        AtomicInteger current = new AtomicInteger();
        for (RevCommit commit : commits) {
            List<RevCommit> finalCommits = commits;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                state.addToState(key, "Analysing finding from commit " + current.getAndIncrement() + "/" + finalCommits.size());
                FindingsChurn churn = findingsChurnGetter.getFindingsChurn(repoOwner, repoName, commit.getCommitTime() * 1000L);
                synchronized (alreadyAnalyzed) {
                    if (!alreadyAnalyzed.contains(churn.getCommit().getTimestamp())) {
                        alreadyAnalyzed.add(churn.getCommit().getTimestamp());
                        churns.add(churn);
                    }
                }
            }, executorService);
            futures.add(future);
        }


        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        System.out.println(alreadyAnalyzed.size());

        //Sort churns by date
        churns.sort((o1, o2) -> Math.toIntExact(o1.getCommit().getTimestamp() / 1000 - o2.getCommit().getTimestamp() / 1000));

        state.removeFromState(key);

        return new Result(repoOwner, repoName, churns);
    }

}
