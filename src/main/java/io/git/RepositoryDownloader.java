package io.git;

import jakarta.inject.Inject;
import logic.StateHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import util.Constants;
import util.GitUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class RepositoryDownloader {

    private static final Semaphore SEMAPHORE = new Semaphore(Constants.CONCURRENT_REPO_DOWNLOADS);

    private final StateHandler stateHandler;
    private final GitUtil gitUtil;

    @Inject
    public RepositoryDownloader(StateHandler stateHandler, GitUtil gitUtil) {
        this.stateHandler = stateHandler;
        this.gitUtil = gitUtil;
    }

    public Git getRepo(String repoOwner, String repoName, String defaultBranch) throws GitAPIException, IOException {
        String folderName = "temp" + File.separator + "repos" + File.separator + repoOwner + File.separator + repoName;
        File folder = new File(folderName);

        Git git;
        if (folder.exists()) {
            git = openRepoAndGoToDefaultBranch(repoOwner, repoName, folderName, defaultBranch);
        } else {
            git = cloneRepo(repoOwner, repoName, folderName);
        }

        // Go to latest commit before Constants.ANALYSIS_TIME
        Iterable<RevCommit> call = git.log().call();
        for (RevCommit commit : call) {
            if (commit.getCommitTime() < Constants.ANALYSIS_TIME) {
                gitUtil.checkout(git, commit.getName());
                break;
            }
        }

        return git;
    }

    public void deleteRepo(String owner, String repoName) throws IOException {
        String folderName = "temp" + File.separator + "repos" + File.separator + owner + File.separator + repoName;
        File folder = new File(folderName);
        FileUtils.deleteDirectory(folder);
    }

    private Git openRepoAndGoToDefaultBranch(String owner, String repoName, String folderName, String defaultBranch) throws GitAPIException, IOException {
        File gitFile = new File(folderName + File.separator + ".git");

        Repository repo = new FileRepositoryBuilder().setGitDir(gitFile).build();
        try (Git git = Git.wrap(repo)) {
            stateHandler.addToState(Pair.of(owner, repoName), "Opening existing repo");

            gitUtil.checkout(git, defaultBranch);
            git.pull().call();
            return git;
        } catch (Exception e) {
            return cloneRepo(owner, repoName, folderName);
        }
    }

    private Git cloneRepo(String owner, String repoName, String folderName) throws GitAPIException, IOException {
        try {
            stateHandler.addToState(Pair.of(owner, repoName), "Waiting for download");
            SEMAPHORE.acquire();
            stateHandler.addToState(Pair.of(owner, repoName), "Downloading repository");
            File folder = new File(folderName);
            deleteRepo(owner, repoName);
            return Git.cloneRepository().setDirectory(folder).setURI("https://github.com/" + owner + "/" + repoName + ".git").call();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            SEMAPHORE.release();
        }
    }
}
