package logic;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.kohsuke.github.GHRepository;
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
            File gitFile = new File(folderName + File.separator + ".git");
            return openRepoAndGoToDefaultBranch(github, owner, repoName, gitFile);
        }

        return cloneRepo(owner, repoName, folder);
    }

    private Git openRepoAndGoToDefaultBranch(GitHub github, String owner, String repoName, File gitFile) throws IOException, GitAPIException {
        Git git = Git.wrap(new FileRepositoryBuilder().setGitDir(gitFile).build());

        //Go to default branch
        GHRepository repository = github.getRepository(owner + "/" + repoName);
        String defaultBranch = repository.getDefaultBranch();
        git.stashCreate().call();
        git.checkout().setName(defaultBranch).call();

        return git;
    }

    private Git cloneRepo(String owner, String repoName, File folder) throws GitAPIException {
        return Git.cloneRepository().setDirectory(folder).setURI("https://github.com/" + owner + "/" + repoName + ".git").call();
    }
}
