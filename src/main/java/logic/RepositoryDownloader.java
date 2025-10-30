package logic;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;

public class RepositoryDownloader {

    private final GitHub github;

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
        Git git = Git.wrap(repo);

        try {
            GitUtil.prepareCheckout(git);
            String defaultBranch = github.getRepository(owner + "/" + repoName).getDefaultBranch();
            git.checkout().setName(defaultBranch).call();
            return git;
        } catch (Exception e) {
            return cloneRepo(owner, repoName, folderName);
        }

    }

    private synchronized Git cloneRepo(String owner, String repoName, String folderName) throws GitAPIException, IOException {
        File folder = new File(folderName);
        FileUtils.deleteDirectory(folder);

        System.out.println("Start cloning: " + owner + "/" + repoName);
        Git git = Git.cloneRepository().setDirectory(folder).setURI("https://github.com/" + owner + "/" + repoName + ".git").call();
        System.out.println("End cloning: " + owner + "/" + repoName);
        return git;
    }
}
