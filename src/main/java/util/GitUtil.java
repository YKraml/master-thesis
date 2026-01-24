package util;

import jakarta.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitUtil {

    @Inject
    public GitUtil() {

    }

    public Git checkout(Git git, String commit) throws GitAPIException {
        git.clean().setForce(true).setCleanDirectories(true).call();
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
        git.checkout().setName(commit).call();
        return git;
    }
}
