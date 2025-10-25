import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final String TOKEN = "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";

    public static void main(String[] args) throws IOException, GitAPIException {

        String owner = "YKraml";
        String repoName = "GameOfLive";

        GitHub github = new GitHubBuilder().withOAuthToken(TOKEN).build();

        try (Git git = getRepo(github, owner, repoName)) {
            Iterable<RevCommit> call = git.log().call();

            List<RevCommit> commits = new ArrayList<>();
            call.forEach(commits::add);

            for (RevCommit commit : commits) {
                System.out.println(commit.getName());
            }

            git.checkout().setName("865a33b21ee6d9160e0eb9256d88163b7832b3d9").call();
        }

    }


    private static Git getRepo(GitHub github, String owner, String repoName) throws GitAPIException, IOException {
        String folderName = "repos" + File.separator + owner + File.separator + repoName;
        File folder = new File(folderName);

        if (folder.exists()) {
            File gitFile = new File(folderName + File.separator + ".git");
            return openRepoAndGoToDefaultBranch(github, owner, repoName, gitFile);
        }

        return cloneRepo(owner, repoName, folder);
    }

    private static Git openRepoAndGoToDefaultBranch(GitHub github, String owner, String repoName, File gitFile) throws IOException, GitAPIException {
        Git repo = Git.wrap(new FileRepositoryBuilder()
                .setGitDir(gitFile)
                .build());

        //Go to default branch
        GHRepository repository = github.getRepository(owner + "/" + repoName);
        String defaultBranch = repository.getDefaultBranch();
        repo.checkout().setName(defaultBranch).call();

        return repo;
    }

    private static Git cloneRepo(String owner, String repoName, File folder) throws GitAPIException {
        return Git.cloneRepository()
                .setDirectory(folder)
                .setURI("https://github.com/" + owner + "/" + repoName + ".git")
                .call();
    }
}
