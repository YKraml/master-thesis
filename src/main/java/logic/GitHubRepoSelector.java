package logic;

import io.json.GitHubRepositoryReaderWriter;
import jakarta.inject.Inject;
import model.writable.GitHubRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositorySearchBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;
import util.Constants;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class GitHubRepoSelector {

    private final GitHub github;
    private final StateHandler stateHandler;
    private final GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter;

    @Inject
    public GitHubRepoSelector(GitHub github, StateHandler stateHandler, GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter) {
        this.github = github;
        this.stateHandler = stateHandler;
        this.gitHubRepositoryReaderWriter = gitHubRepositoryReaderWriter;
    }

    public void start(int numberOfRepos) throws IOException, InterruptedException {
        AtomicInteger currentNumberOfStars = new AtomicInteger(Integer.MAX_VALUE);

        int i = 0;
        outer:
        while (true) {
            PagedSearchIterable<GHRepository> repositories = github
                    .searchRepositories()
                    .language(Constants.CURRENT_LANGUAGE.name().toLowerCase())
                    .stars("<=" + currentNumberOfStars)
                    .sort(GHRepositorySearchBuilder.Sort.STARS)
                    .list();

            for (GHRepository repository : repositories) {
                int stargazersCount = repository.getStargazersCount();
                String repoOwner = repository.getOwnerName();
                String repoName = repository.getName();
                String defaultBranch = repository.getDefaultBranch();
                int size = repository.getSize();

                currentNumberOfStars.set(stargazersCount);

                if (gitHubRepositoryReaderWriter.read(repoOwner, repoName).isPresent()) {
                    continue;
                }

                stateHandler.addToState(Pair.of(repoOwner, repoName), "Added");
                gitHubRepositoryReaderWriter.write(new GitHubRepository(repoOwner, repoName, stargazersCount, defaultBranch, size));
                stateHandler.removeFromState(Pair.of(repoOwner, repoName));

                Thread.sleep(500);

                i++;
                if (i >= numberOfRepos) {
                    break outer;
                }
            }
        }
    }
}
