package logic;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.kohsuke.github.GitHub;
import util.GitUtil;
import util.State;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class RepositoryDownloader {

    private final GitHub github;
    private static final Semaphore semaphore = new Semaphore(4);

    public RepositoryDownloader(GitHub github) {
        this.github = github;
    }

    public Git getRepo(String owner, String repoName) throws GitAPIException, IOException {
        String folderName = "repos" + File.separator + owner + File.separator + repoName;
        File folder = new File(folderName);

        if (folder.exists()) {
            return openRepoAndGoToDefaultBranch(github, owner, repoName, folderName);
        }

        return cloneRepo(owner, repoName, folderName);
    }

    private Git openRepoAndGoToDefaultBranch(GitHub github, String owner, String repoName, String folderName) throws GitAPIException, IOException {
        File gitFile = new File(folderName + File.separator + ".git");
        Repository repo = new FileRepositoryBuilder().setGitDir(gitFile).build();
        try (Git git = Git.wrap(repo)) {
            GitUtil.prepareCheckout(git);
            String defaultBranch = github.getRepository(owner + "/" + repoName).getDefaultBranch();
            git.checkout().setName(defaultBranch).call();
            return git;
        } catch (Exception e) {
            return cloneRepo(owner, repoName, folderName);
        }
    }

    private Git cloneRepo(String owner, String repoName, String folderName) throws GitAPIException {
        try {
            State.getInstance().addToState(Pair.of(owner, repoName), "Waiting for download");
            semaphore.acquire();
            State.getInstance().addToState(Pair.of(owner, repoName), "Downloading");
            File folder = new File(folderName);
            deleteRepo(owner, repoName);
            return Git.cloneRepository().setDirectory(folder).setURI("https://github.com/" + owner + "/" + repoName + ".git").call();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public void deleteRepo(String owner, String repoName) {
        String folderName = "repos" + File.separator + owner + File.separator + repoName;
        File folder = new File(folderName);
        try {
            FileUtils.deleteDirectory(folder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
