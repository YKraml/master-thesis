package io.git;

import jakarta.inject.Inject;
import logic.StateHandler;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.PackProtocolException;
import org.eclipse.jgit.transport.FilterSpec;
import util.Constants;
import util.GitUtil;

import java.io.File;
import java.util.concurrent.Semaphore;

public class BareRepositoryDownloader extends RepositoryDownloader {

    private static final Semaphore SEMAPHORE = new Semaphore(Constants.CONCURRENT_BARE_REPO_DOWNLOADS);

    @Inject
    public BareRepositoryDownloader(StateHandler stateHandler, GitUtil gitUtil) {
        super(gitUtil, stateHandler);
    }

    @Override
    protected Semaphore getSemaphore() {
        return SEMAPHORE;
    }

    @Override
    protected Git goToLatestCommit(Git git) {
        // do nothing
        return git;
    }

    @Override
    protected String getFolderName(String repoOwner, String repoName) {
        return "temp" + File.separator + "repos_bare" + File.separator + repoOwner + File.separator + repoName;
    }

    @Override
    protected Git executeClone(String repoOwner, String repoName, String folderName) throws GitAPIException {
        return Git.cloneRepository()
                .setDirectory(new File(folderName))
                .setBare(true)
                .setURI("https://github.com/" + repoOwner + "/" + repoName + ".git")
                .setTransportConfigCallback(transport -> {
                    try {
                        transport.setFilterSpec(FilterSpec.fromFilterLine("tree:0"));
                    } catch (PackProtocolException e) {
                        throw new RuntimeException(e);
                    }
                })
                .call();
    }

}
