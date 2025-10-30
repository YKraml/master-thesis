package logic;

import model.Linter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinterIntroductionFinder {

    private final LinterFolderFinder linterFolderFinder;

    public LinterIntroductionFinder(LinterFolderFinder linterFolderFinder) {
        this.linterFolderFinder = linterFolderFinder;
    }

    public Pair<RevCommit, String> findIntroduction(Git git, Linter linter) throws GitAPIException {
        Iterable<RevCommit> call = git.log().call();

        //First entry in list is the latest commit
        List<RevCommit> commits = new ArrayList<>();
        call.forEach(commits::add);
        commits = commits.reversed();

        //Binary search for the introduction commit
        Pair<RevCommit, String> revCommit = doBinarySearch(git, linter, commits);

        GitUtil.prepareCheckout(git);
        git.checkout().setName(commits.getLast().getName()).call();

        return revCommit;
    }

    private Pair<RevCommit, String> doBinarySearch(Git git, Linter linter, List<RevCommit> commits) throws GitAPIException {
        int top = commits.size() - 1;
        int bottom = 0;
        do {
            int halfWay = bottom + (top - bottom) / 2;
            GitUtil.prepareCheckout(git);
            git.checkout().setName(commits.get(halfWay).getName()).call();
            File folder = git.getRepository().getDirectory().getParentFile();
            if (searchLinterInFolder(linter, folder).isPresent()) {
                top = halfWay;
            } else {
                bottom = halfWay;
            }

        } while (top - 1 != bottom);

        // Check bottom and top commit for linter
        int commitIndex;
        if (searchLinterInRepo(git, linter, commits.get(bottom).getName()).isPresent()) {
            commitIndex = bottom;
        } else if (searchLinterInRepo(git, linter, commits.get(top).getName()).isPresent()) {
            commitIndex = top;
        } else {
            throw new RuntimeException("Linter introduction not found.");
        }


        //Check commit before introduction commit. It should not contain the linter.
        if (commitIndex > 0 && searchLinterInRepo(git, linter, commits.get(commitIndex - 1).getName()).isPresent()) {
            throw new RuntimeException("Linter found, but not expected.");
        }

        //Check commit after introduction commit. It should contain the linter.
        if (commitIndex < commits.size() - 1 && searchLinterInRepo(git, linter, commits.get(commitIndex + 1).getName()).isEmpty()) {
            throw new RuntimeException("Linter expected, but not found.");
        }

        return Pair.of(commits.get(commitIndex), searchLinterInRepo(git, linter, commits.get(commitIndex).getName()).get());
    }

    private Optional<String> searchLinterInRepo(Git git, Linter linter, String commitName) throws GitAPIException {
        GitUtil.prepareCheckout(git);
        git.checkout().setName(commitName).call();

        File folder = git.getRepository().getDirectory().getParentFile();
        return searchLinterInFolder(linter, folder);
    }

    private Optional<String> searchLinterInFolder(Linter linter, File folder) {
        for (LinterFileFinder.Result result : linterFolderFinder.findLinters(folder)) {
            if (result.linterName().equals(linter.name())) {
                return Optional.ofNullable(result.fileName());
            }
        }

        return Optional.empty();
    }

}
