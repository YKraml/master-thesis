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
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LinterIntroductionFinder {

    private final LinterFolderFinder linterFolderFinder;
    private final GitHub gitHub;

    public LinterIntroductionFinder(LinterFolderFinder linterFolderFinder, GitHub gitHub) {
        this.linterFolderFinder = linterFolderFinder;
        this.gitHub = gitHub;
    }

    public Pair<RevCommit, String> findIntroduction(Git git, Linter linter) throws IOException, GitAPIException {
        Iterable<RevCommit> topologicalLog = getTopologicalLog(git.getRepository());
        List<RevCommit> commits = new ArrayList<>();
        topologicalLog.forEach(commits::add);
        //commits = commits.reversed();

        //commits.sort(new Comparator<RevCommit>() {
        //    @Override
        //    public int compare(RevCommit o1, RevCommit o2) {
        //        return o1.getCommitTime() - o2.getCommitTime();
        //    }
        //});
        //for (int i = 0; i < commits.size() - 1; i++) {
        //    if (commits.get(i).getCommitTime() > commits.get(i + 1).getCommitTime()) {
        //        System.out.println(i + " " + (i + 1));
        //          throw new RuntimeException();
        //    }
        //}

        String name = commits.get(1000).getName();

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

    public Iterable<RevCommit> getTopologicalLog(Repository repository) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            ObjectId headId = repository.resolve("HEAD");
            walk.markStart(walk.parseCommit(headId));
            walk.sort(RevSort.TOPO_KEEP_BRANCH_TOGETHER);
            return walk;
        }
    }

}
