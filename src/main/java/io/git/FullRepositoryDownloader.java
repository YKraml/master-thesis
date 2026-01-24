package io.git;

import jakarta.inject.Inject;
import logic.StateHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import util.Constants;
import util.GitUtil;

import java.io.File;
import java.util.concurrent.Semaphore;

public class FullRepositoryDownloader extends RepositoryDownloader {

    private static final Semaphore SEMAPHORE = new Semaphore(Constants.CONCURRENT_REPO_DOWNLOADS);

    @Inject
    public FullRepositoryDownloader(StateHandler stateHandler, GitUtil gitUtil) {
        super(gitUtil, stateHandler);
    }

    @Override
    public String getFolderName(String repoOwner, String repoName) {
        return "temp" + File.separator + "repos_full" + File.separator + repoOwner + File.separator + repoName;
    }

    @Override
    protected Semaphore getSemaphore() {
        return SEMAPHORE;
    }

    @Override
    protected Git goToLatestCommit(Git git) throws GitAPIException {
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

    @Override
    protected Git executeClone(String owner, String repoName, String folderName) throws GitAPIException {
        return Git.cloneRepository()
                .setDirectory(new File(folderName))
                .setURI("https://github.com/" + owner + "/" + repoName + ".git")
                .call();
    }
}
