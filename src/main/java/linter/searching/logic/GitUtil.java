package linter.searching.logic;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitUtil {

    public static void checkout(Git git, String commit) throws GitAPIException {
        prepareCheckout(git);
        git.checkout().setName(commit).call();
    }

    private static void prepareCheckout(Git git) throws GitAPIException {
        git.clean().setForce(true).setCleanDirectories(true).call();
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
    }
}
