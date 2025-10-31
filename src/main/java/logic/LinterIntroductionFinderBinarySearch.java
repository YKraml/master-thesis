package logic;

import model.Linter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LinterIntroductionFinderBinarySearch implements LinterIntroductionFinder {

    private final LinterFolderFinder linterFolderFinder;

    public LinterIntroductionFinderBinarySearch(LinterFolderFinder linterFolderFinder) {
        this.linterFolderFinder = linterFolderFinder;
    }

    @Override
    public Pair<RevCommit, String> findIntroduction(Git git, Linter linter) throws IOException, GitAPIException {
        Iterable<RevCommit> topologicalLog = getTopologicalLog(git.getRepository());
        List<RevCommit> commits = new ArrayList<>();
        topologicalLog.forEach(commits::add);
        commits = commits.reversed();

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
            if (searchLinterInFolder(linter, git.getRepository().getDirectory().getParentFile(), linterFolderFinder).isPresent()) {
                top = halfWay;
            } else {
                bottom = halfWay;
            }

        } while (top - 1 != bottom);

        // Check bottom and top commit for linter
        int commitIndex;
        if (searchLinterInRepo(git, linter, commits.get(bottom).getName(), linterFolderFinder).isPresent()) {
            commitIndex = bottom;
        } else if (searchLinterInRepo(git, linter, commits.get(top).getName(), linterFolderFinder).isPresent()) {
            commitIndex = top;
        } else {
            throw new RuntimeException("Linter introduction not found.");
        }


        //Check commit before introduction commit. It should not contain the linter.
        if (commitIndex > 0 && searchLinterInRepo(git, linter, commits.get(commitIndex - 1).getName(), linterFolderFinder).isPresent()) {
            throw new RuntimeException("Linter found, but not expected.");
        }

        //Check commit after introduction commit. It should contain the linter.
        if (commitIndex < commits.size() - 1 && searchLinterInRepo(git, linter, commits.get(commitIndex + 1).getName(), linterFolderFinder).isEmpty()) {
            throw new RuntimeException("Linter expected, but not found.");
        }

        return Pair.of(commits.get(commitIndex), searchLinterInRepo(git, linter, commits.get(commitIndex).getName(), linterFolderFinder).get());
    }


    public Iterable<RevCommit> getTopologicalLog(Repository repository) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            ObjectId headId = repository.resolve("HEAD");
            RevCommit head = walk.parseCommit(headId);
            walk.markStart(head);
            walk.sort(RevSort.TOPO);
            walk.sort(RevSort.COMMIT_TIME_DESC, true);
            return walk;
        }
    }

}
