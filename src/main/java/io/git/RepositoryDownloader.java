package io.git;

import jakarta.inject.Inject;
import logic.StateHandler;
import model.CemProject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import util.GitUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

public abstract class RepositoryDownloader {

    protected final GitUtil gitUtil;
    protected final StateHandler stateHandler;

    @Inject
    public RepositoryDownloader(GitUtil gitUtil, StateHandler stateHandler) {
        this.gitUtil = gitUtil;
        this.stateHandler = stateHandler;
    }

    public Git getRepo(String repoOwner, String repoName, String defaultBranch) throws GitAPIException, IOException {
        String folderName = getFolderName(repoOwner, repoName);
        File folder = new File(folderName);

        Git git;
        if (folder.exists()) {
            git = openRepoAndGoToDefaultBranch(repoOwner, repoName, folderName, defaultBranch);
        } else {
            git = cloneRepo(repoOwner, repoName, folderName);
        }

        return goToLatestCommit(git);
    }

    public void deleteRepo(String owner, String repoName) throws IOException {
        String folderName = getFolderName(owner, repoName);
        File folder = new File(folderName);
        FileUtils.deleteDirectory(folder);
    }

    public List<CemProject.Commit> getCommits(String repoOwner, String repoName, String defaultBranch) {
        List<CemProject.Commit> commits = new ArrayList<>();
        try {
            Git git = getRepo(repoOwner, repoName, defaultBranch);
            Iterator<RevCommit> iterator = git.log().call().iterator();
            List<RevCommit> revCommits = new ArrayList<>();
            iterator.forEachRemaining(revCommits::add);
            List<RevCommit> reversed = revCommits.reversed();
            for (int i = 0; i < reversed.size(); i++) {
                RevCommit commit = reversed.get(i);
                commits.add(new CemProject.Commit(commit.getName(), commit.getCommitTime(), i));
            }
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
        }
        return commits;
    }

    protected Git cloneRepo(String repoOwner, String repoName, String folderName) throws GitAPIException, IOException {
        try {
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Waiting for download");
            getSemaphore().acquire();
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Downloading repository");
            deleteRepo(repoOwner, repoName);
            return executeClone(repoOwner, repoName, folderName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            getSemaphore().release();
        }
    }

    private Git openRepoAndGoToDefaultBranch(String owner, String repoName, String folderName, String defaultBranch) throws GitAPIException, IOException {
        File gitFile = new File(folderName + File.separator + ".git");

        Repository repo = new FileRepositoryBuilder().setGitDir(gitFile).build();
        try (Git git = Git.wrap(repo)) {
            stateHandler.addToState(Pair.of(owner, repoName), "Opening existing repo");

            return gitUtil.checkout(git, defaultBranch);
        } catch (Exception e) {
            return cloneRepo(owner, repoName, folderName);
        }
    }

    protected abstract Semaphore getSemaphore();

    protected abstract Git goToLatestCommit(Git git) throws GitAPIException;

    protected abstract String getFolderName(String repoOwner, String repoName);

    protected abstract Git executeClone(String owner, String repoName, String folderName) throws GitAPIException;
}
