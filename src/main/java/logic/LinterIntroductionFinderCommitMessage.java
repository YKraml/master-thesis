package logic;

import model.Linter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinterIntroductionFinderCommitMessage implements LinterIntroductionFinder {

    private final LinterFolderFinder linterFolderFinder;

    public LinterIntroductionFinderCommitMessage(LinterFolderFinder linterFolderFinder) {
        this.linterFolderFinder = linterFolderFinder;
    }

    @Override
    public Pair<RevCommit, String> findIntroduction(Git git, Linter linter) throws GitAPIException {
        Iterable<RevCommit> topologicalLog = git.log().call();
        List<RevCommit> commits = new ArrayList<>();
        topologicalLog.forEach(commits::add);
        commits = commits.reversed();


        for (RevCommit commit : commits) {
            boolean contained = commit.getFullMessage().toLowerCase().contains(linter.name().toLowerCase());
            if (contained) {
                Optional<String> fileName = searchLinterInRepo(git, linter, commit.getName(), linterFolderFinder);
                if (fileName.isPresent()) {
                    return Pair.of(commit, fileName.get());
                }
            }
        }

        throw new RuntimeException("Linter introduction not found");
    }


}
