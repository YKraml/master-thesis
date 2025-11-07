package linter.searching.logic;

import common.Result;
import common.ResultReaderWriter;
import jakarta.inject.Inject;
import linter.searching.model.Language;
import linter.searching.model.Linter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.github.GHRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RepositoryHandler {

    public static final int SIZE_1 = 1000 * 1000; // 1 GB
    public static final int SIZE_2 = 1000 * 1000 * 1000; // 1 GB

    private final RepositoryDownloader repositoryDownloader;
    private final LinterFinder linterFinder;
    private final LinterIntroductionFinder linterIntroductionFinder;
    private final ResultReaderWriter resultReaderWriter;
    private final State state;

    @Inject
    public RepositoryHandler(RepositoryDownloader repositoryDownloader, LinterFinder linterFinder, LinterIntroductionFinder linterIntroductionFinder, ResultReaderWriter resultReaderWriter, State state) {
        this.repositoryDownloader = repositoryDownloader;
        this.linterFinder = linterFinder;
        this.linterIntroductionFinder = linterIntroductionFinder;
        this.resultReaderWriter = resultReaderWriter;
        this.state = state;
    }

    public void handleRepo(GHRepository repo) throws Exception {
        String ownerName = repo.getOwnerName();
        String repoName = repo.getName();
        state.addToState(Pair.of(ownerName, repoName), "Started handling");

        //Limit at around 0.5GB. The getSize() is not very accurate.
        if (repo.getSize() > SIZE_1) {
            return;
        }

        //Skip if already analysed
        File proofFile = resultReaderWriter.getResultFile(ownerName, repoName);
        if (proofFile.exists()) {
            return;
        }

        Optional<Result> result = handleRepo(ownerName, repoName);
        if (result.isPresent()) {
            resultReaderWriter.writeResult(result.get());
        }
    }

    public Optional<Result> handleRepo(String ownerName, String repoName) throws GitAPIException, IOException {
        try (Git git = repositoryDownloader.getRepo(ownerName, repoName)) {
            File folder = git.getRepository().getDirectory().getParentFile();
            RevCommit currentCommit = git.log().call().iterator().next();
            String currentBranch = git.getRepository().getBranch();

            //Limit at 0.5GB. The getSize() is not very accurate.
            long size = FileUtils.sizeOfDirectory(folder);
            if (size > SIZE_2) {
                return Optional.empty();
            }

            state.addToState(Pair.of(ownerName, repoName), "Searching for linter");
            List<LinterFinder.Result> currentLinters = linterFinder.findLinters(folder);

            List<Result.LinterWithProof> lintersWithProof = new ArrayList<>();
            for (LinterFinder.Result linter : currentLinters) {
                state.addToState(Pair.of(ownerName, repoName), "Searching introduction of: " + linter.linterName());
                Pair<RevCommit, String> introduction = linterIntroductionFinder.findIntroductionCommitWithProof(git, Linter.valueOf(linter.linterName()));
                lintersWithProof.add(new Result.LinterWithProof(linter.linterName(), linter.fileName(), introduction.getKey().getName(), introduction.getValue(), introduction.getLeft().getCommitTime()));
            }

            return Optional.of(new Result(ownerName, repoName, Language.JAVA, "https://github.com/" + ownerName + "/" + repoName, currentBranch, currentCommit.getName(), currentCommit.getCommitTime(), lintersWithProof));
        }
    }
}
