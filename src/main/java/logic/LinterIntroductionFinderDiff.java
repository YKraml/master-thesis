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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinterIntroductionFinderDiff implements LinterIntroductionFinder {

    private final LinterFolderFinder linterFolderFinder;

    public LinterIntroductionFinderDiff(LinterFolderFinder linterFolderFinder) {
        this.linterFolderFinder = linterFolderFinder;
    }


    @Override
    public Pair<RevCommit, String> findIntroduction(Git git, Linter linter) throws IOException, GitAPIException {
        Iterable<RevCommit> topologicalLog = git.log().call();
        List<RevCommit> commits = new ArrayList<>();
        topologicalLog.forEach(commits::add);
        commits = commits.reversed();

        DiffFormatter formatter1 = new DiffFormatter(DisabledOutputStream.INSTANCE);
        Repository repository = git.getRepository();
        formatter1.setRepository(repository);

        RevCommit commitThatIntroducedLinter = null;
        String fileName = null;

        int i = 0;
        outer:
        //Go through all commits
        for (RevCommit commit : commits) {
            ObjectReader reader = repository.newObjectReader();
            AbstractTreeIterator oldTreeIter;
            if (commit.getParentCount() == 0) {
                oldTreeIter = new EmptyTreeIterator();
            } else {
                RevCommit parent = commit.getParent(0);
                oldTreeIter = new CanonicalTreeParser();
                ((CanonicalTreeParser) oldTreeIter).reset(reader, parent.getTree());
            }

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, commit.getTree());

            //Go through all diff entries (one entry per file)
            for (DiffEntry entry : formatter1.scan(oldTreeIter, newTreeIter)) {
                i++;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                DiffFormatter formatter2 = new DiffFormatter(outputStream);
                formatter2.setRepository(repository);
                formatter2.format(entry);
                String diffText = outputStream.toString(StandardCharsets.UTF_8);

                //Go through all indicators of linter
                for (FileIndicator indicator : linter.getIndicators()) {

                    boolean test = indicator.test(entry.getChangeType(), diffText);
                    if (test) {
                        Optional<String> fileNameOptional = searchLinterInRepo(git, linter, commit.getName(), linterFolderFinder);
                        if (fileNameOptional.isPresent()) {
                            commitThatIntroducedLinter = commit;
                            fileName = fileNameOptional.get();
                            break outer;
                        }
                    }
                }


            }


        }

        if (commitThatIntroducedLinter == null) {
            throw new RuntimeException("Linter introduction not found");
        }

        GitUtil.prepareCheckout(git);
        git.checkout().setName(commits.getLast().getName()).call();

        return Pair.of(commitThatIntroducedLinter, fileName);
    }
}
