package io.git;

import jakarta.inject.Inject;
import logic.StateHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.kohsuke.github.GitHub;
import util.Constants;
import util.GitUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class RepositoryDownloader {

    private static final Semaphore semaphore = new Semaphore(Constants.CONCURRENT_REPO_DOWNLOADS);

    private final GitHub github;
    private final StateHandler stateHandler;
    private final GitUtil gitUtil;

    @Inject
    public RepositoryDownloader(GitHub github, StateHandler stateHandler, GitUtil gitUtil) {
        this.github = github;
        this.stateHandler = stateHandler;
        this.gitUtil = gitUtil;
    }

    public Git getRepo(String repoOwner, String repoName) throws GitAPIException, IOException {
        String folderName = "temp" + File.separator + "repos" + File.separator + repoOwner + File.separator + repoName;
        File folder = new File(folderName);

        if (folder.exists()) {
            return openRepoAndGoToDefaultBranch(github, repoOwner, repoName, folderName);
        }

        return cloneRepo(repoOwner, repoName, folderName);
    }

    private Git openRepoAndGoToDefaultBranch(GitHub github, String owner, String repoName, String folderName) throws GitAPIException, IOException {
        File gitFile = new File(folderName + File.separator + ".git");
        String defaultBranch = github.getRepository(owner + "/" + repoName).getDefaultBranch();

        Repository repo = new FileRepositoryBuilder().setGitDir(gitFile).build();
        try (Git git = Git.wrap(repo)) {
            stateHandler.addToState(Pair.of(owner, repoName), "Opening existing repo");

            gitUtil.checkout(git, defaultBranch);
            git.pull();
            return git;
        } catch (Exception e) {
            return cloneRepo(owner, repoName, folderName);
        }
    }

    private Git cloneRepo(String owner, String repoName, String folderName) throws GitAPIException, IOException {
        try {
            stateHandler.addToState(Pair.of(owner, repoName), "Waiting for download");
            semaphore.acquire();
            stateHandler.addToState(Pair.of(owner, repoName), "Downloading repository");
            File folder = new File(folderName);
            deleteRepo(owner, repoName);
            return Git.cloneRepository().setDirectory(folder).setURI("https://github.com/" + owner + "/" + repoName + ".git").call();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public void deleteRepo(String owner, String repoName) throws IOException {
        String folderName = "temp" + File.separator + "repos" + File.separator + owner + File.separator + repoName;
        File folder = new File(folderName);
        FileUtils.deleteDirectory(folder);
    }
}
