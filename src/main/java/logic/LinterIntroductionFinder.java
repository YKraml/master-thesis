package logic;

import model.Linter;
import model.Proof;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LinterIntroductionFinder {

    private final LinterFolderFinder linterFolderFinder;

    public LinterIntroductionFinder(LinterFolderFinder linterFolderFinder) {
        this.linterFolderFinder = linterFolderFinder;
    }

    public Optional<String> findIntroduction(Git git, Linter linter) throws GitAPIException {
        Iterable<RevCommit> call = git.log().call();

        //First entry in list is the latest commit
        List<RevCommit> commits = new ArrayList<>();
        call.forEach(commits::add);
        commits = commits.reversed();

        //Binary search for the introduction commit
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
        RevCommit bottomCommit = commits.get(bottom);
        RevCommit topCommit = commits.get(top);
        String commit = null;
        if (isLinterPresent(git, linter, bottomCommit)) {
            commit = bottomCommit.getName();
        } else if (isLinterPresent(git, linter, topCommit)) {
            commit = topCommit.getName();
        }

        git.stashCreate().call();
        git.checkout().setName(commits.getLast().getName()).call();
        return Optional.ofNullable(commit);
    }

    private boolean isLinterPresent(Git git, Linter linter, RevCommit targetCommit) throws GitAPIException {
        git.stashCreate().call();
        git.checkout().setName(targetCommit.getName()).call();
        File folder = git.getRepository().getDirectory().getParentFile();
        return linterFolderFinder.findLinters(folder).containsKey(linter);
    }
}
