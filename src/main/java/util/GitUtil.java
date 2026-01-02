package util;

import jakarta.inject.Inject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitUtil {

    @Inject
    public GitUtil() {

    }

    public void checkout(Git git, String commit) throws GitAPIException {
        prepareCheckout(git);
        git.checkout().setName(commit).call();
    }

    private void prepareCheckout(Git git) throws GitAPIException {
        git.clean().setForce(true).setCleanDirectories(true).call();
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
    }
}
