package logic;

import model.Linter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LinterIntroductionFinder {

    private final LinterFolderFinder linterFolderFinder;

    public LinterIntroductionFinder(LinterFolderFinder linterFolderFinder) {
        this.linterFolderFinder = linterFolderFinder;
    }

    public RevCommit findIntroduction(Git git, Linter linter) throws GitAPIException {
        Iterable<RevCommit> call = git.log().call();

        //First entry in list is the latest commit
        List<RevCommit> commits = new ArrayList<>();
        call.forEach(commits::add);
        commits = commits.reversed();

        //Binary search for the introduction commit
        RevCommit revCommit = doBinarySearch(git, linter, commits);

        git.stashCreate().call();
        git.checkout().setName(commits.getLast().getName()).call();

        return revCommit;
    }

    private RevCommit doBinarySearch(Git git, Linter linter, List<RevCommit> commits) throws GitAPIException {
        int top = commits.size() - 1;
        int bottom = 0;
        do {
            int halfWay = bottom + (top - bottom) / 2;
            git.stashCreate().call();
            git.checkout().setName(commits.get(halfWay).getName()).call();
            File folder = git.getRepository().getDirectory().getParentFile();
            if (linterFolderFinder.findLinters(folder).containsKey(linter)) {
                top = halfWay;
            } else {
                bottom = halfWay;
            }

        } while (top - 1 != bottom);

        // Check bottom and top commit for linter
        int commitIndex;
        if (isLinterPresent(git, linter, commits.get(bottom))) {
            commitIndex = bottom;
        } else if (isLinterPresent(git, linter, commits.get(top))) {
            commitIndex = top;
        } else {
            throw new RuntimeException("Linter introduction not found.");
        }


        //Check commit before introduction commit. It should not contain the linter.
        if (commitIndex > 0 & isLinterPresent(git, linter, commits.get(commitIndex - 1))) {
            throw new RuntimeException("Linter found, but not expected.");
        }


        return commits.get(commitIndex);
    }

    private boolean isLinterPresent(Git git, Linter linter, RevCommit targetCommit) throws GitAPIException {
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
        git.stashCreate().call();
        git.checkout().setName(targetCommit.getName()).call();
        File folder = git.getRepository().getDirectory().getParentFile();
        return linterFolderFinder.findLinters(folder).containsKey(linter);
    }
}
