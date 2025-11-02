package logic;

import model.Linter;
import model.indicators.FileIndicator;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import util.GitUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class LinterIntroductionFinderDiff implements LinterIntroductionFinder {

    private final LinterFinder linterFinder;

    public LinterIntroductionFinderDiff(LinterFinder linterFinder) {
        this.linterFinder = linterFinder;
    }

    @Override
    public Pair<RevCommit, String> findIntroductionCommitWithProof(Git git, Linter linter) throws IOException, GitAPIException {
        Iterable<RevCommit> topologicalLog = git.log().call();
        List<RevCommit> commits = new LinkedList<>();
        topologicalLog.forEach(commits::addFirst);

        Optional<Pair<RevCommit, String>> introductionCommitWithProof = findIntroductionCommitWithProof(git, linter, commits);
        if (introductionCommitWithProof.isEmpty()) {
            throw new RuntimeException("Linter introduction not found");
        }

        GitUtil.prepareCheckout(git);
        git.checkout().setName(commits.getLast().getName()).call();

        return introductionCommitWithProof.get();
    }

    private Optional<Pair<RevCommit, String>> findIntroductionCommitWithProof(Git git, Linter linter, List<RevCommit> commits) throws IOException, GitAPIException {
        for (RevCommit commit : commits) {
            for (DiffEntry diff : getCommitDiffs(git, commit)) {
                Optional<Pair<RevCommit, String>> introductionCommitWithProof = checkDiffWithIndicators(git, linter, commit, diff);
                if (introductionCommitWithProof.isPresent()) {
                    return introductionCommitWithProof;
                }
            }
        }

        return Optional.empty();
    }

    private Optional<Pair<RevCommit, String>> checkDiffWithIndicators(Git git, Linter linter, RevCommit commit, DiffEntry diff) throws IOException, GitAPIException {
        String diffText = getDiffText(git, diff);
        for (FileIndicator indicator : linter.getIndicators()) {
            if (indicator.test(diff.getChangeType(), diffText)) {
                Optional<String> fileNameOptional = searchLinterInRepo(git, linter, commit.getName(), linterFinder);
                if (fileNameOptional.isPresent()) {
                    return Optional.of(Pair.of(commit, fileNameOptional.get()));
                }
            }
        }
        return Optional.empty();
    }

    private static String getDiffText(Git git, DiffEntry entry) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DiffFormatter formatter = new DiffFormatter(outputStream);
        formatter.setRepository(git.getRepository());
        formatter.format(entry);
        return outputStream.toString(StandardCharsets.ISO_8859_1);
    }

    private static List<DiffEntry> getCommitDiffs(Git git, RevCommit commit) throws IOException {
        Repository repository = git.getRepository();
        DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        formatter.setRepository(repository);
        ObjectReader reader = repository.newObjectReader();
        AbstractTreeIterator oldTreeIterator = getOldTreeIterator(commit, reader);
        CanonicalTreeParser newTreeIterator = getNewCommitIterator(commit, reader);
        return formatter.scan(oldTreeIterator, newTreeIterator);
    }

    private static CanonicalTreeParser getNewCommitIterator(RevCommit commit, ObjectReader reader) throws IOException {
        CanonicalTreeParser newTreeIterator = new CanonicalTreeParser();
        newTreeIterator.reset(reader, commit.getTree());
        return newTreeIterator;
    }

    private static AbstractTreeIterator getOldTreeIterator(RevCommit commit, ObjectReader reader) throws IOException {
        AbstractTreeIterator oldTreeIterator;
        if (commit.getParentCount() == 0) {
            oldTreeIterator = new EmptyTreeIterator();
        } else {
            RevCommit parent = commit.getParent(0);
            oldTreeIterator = new CanonicalTreeParser();
            ((CanonicalTreeParser) oldTreeIterator).reset(reader, parent.getTree());
        }
        return oldTreeIterator;
    }

    private Optional<String> searchLinterInRepo(Git git, Linter linter, String commitName, LinterFinder linterFinder) throws GitAPIException, IOException {
        GitUtil.prepareCheckout(git);
        git.checkout().setName(commitName).call();

        File folder = git.getRepository().getDirectory().getParentFile();
        for (LinterFinder.Result result : linterFinder.findLinters(folder)) {
            if (result.linterName().equals(linter.name())) {
                return Optional.of(result.fileName());
            }
        }

        return Optional.empty();
    }


}
