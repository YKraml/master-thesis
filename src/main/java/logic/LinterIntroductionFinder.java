package logic;

import model.Linter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;

public interface LinterIntroductionFinder {

    Pair<RevCommit, String> findIntroductionCommitWithProof(Git git, Linter linter) throws IOException, GitAPIException;

}
