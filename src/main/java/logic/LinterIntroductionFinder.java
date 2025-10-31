package logic;

import model.Linter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public interface LinterIntroductionFinder {
    Pair<RevCommit, String> findIntroduction(Git git, Linter linter) throws IOException, GitAPIException;

    default Optional<String> searchLinterInRepo(Git git, Linter linter, String commitName, LinterFolderFinder linterFolderFinder) throws GitAPIException {
        GitUtil.prepareCheckout(git);
        git.checkout().setName(commitName).call();

        File folder = git.getRepository().getDirectory().getParentFile();
        return searchLinterInFolder(linter, folder, linterFolderFinder);
    }


    default Optional<String> searchLinterInFolder(Linter linter, File folder, LinterFolderFinder linterFolderFinder) {
        for (LinterFileFinder.Result result : linterFolderFinder.findLinters(folder)) {
            if (result.linterName().equals(linter.name())) {
                return Optional.ofNullable(result.fileName());
            }
        }

        return Optional.empty();
    }
}
