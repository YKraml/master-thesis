package linter.searching.logic;

import common.Proof;
import common.ProofReaderWriter;
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

    private static final int SIZE_1 = 1000 * 1000; // 1 GB
    private static final int SIZE_2 = 1000 * 1000 * 1000; // 1 GB
    private static final int SECONDS_IN_TWO_YEARS = 60 * 60 * 24 * 365 * 2; // two years in seconds

    private final RepositoryDownloader repositoryDownloader;
    private final LinterFinder linterFinder;
    private final LinterIntroductionFinder linterIntroductionFinder;
    private final ProofReaderWriter proofReaderWriter;
    private final State state;

    @Inject
    public RepositoryHandler(RepositoryDownloader repositoryDownloader, LinterFinder linterFinder, LinterIntroductionFinder linterIntroductionFinder, ProofReaderWriter proofReaderWriter, State state) {
        this.repositoryDownloader = repositoryDownloader;
        this.linterFinder = linterFinder;
        this.linterIntroductionFinder = linterIntroductionFinder;
        this.proofReaderWriter = proofReaderWriter;
        this.state = state;
    }

    public void handleRepo(GHRepository repo) throws Exception {
        String ownerName = repo.getOwnerName();
        String repoName = repo.getName();
        state.addToState(Pair.of(ownerName, repoName), "Started handling");

        //Size limit with the data from GitHub
        if (repo.getSize() > SIZE_1) {
            return;
        }

        Optional<Proof> result = handleRepo(ownerName, repoName);
        if (result.isPresent()) {
            proofReaderWriter.writeResult(result.get());
        }
    }

    public Optional<Proof> handleRepo(String ownerName, String repoName) throws GitAPIException, IOException {

        //Skip if already analysed
        File proofFile = proofReaderWriter.getResultFile(ownerName, repoName);
        if (proofFile.exists()) {
            return proofReaderWriter.getResultOfRepo(ownerName, repoName);
        }

        try (Git git = repositoryDownloader.getRepo(ownerName, repoName)) {
            File folder = git.getRepository().getDirectory().getParentFile();
            RevCommit currentCommit = git.log().call().iterator().next();
            String currentBranch = git.getRepository().getBranch();

            //Limit again here after download. The getSize() is not very accurate.
            long size = FileUtils.sizeOfDirectory(folder);
            if (size > SIZE_2) {
                return Optional.empty();
            }

            state.addToState(Pair.of(ownerName, repoName), "Searching for linter");
            List<LinterFinder.Result> currentLinters = linterFinder.findLinters(folder);

            List<Proof.LinterWithProof> lintersWithProof = new ArrayList<>();
            for (LinterFinder.Result linter : currentLinters) {
                state.addToState(Pair.of(ownerName, repoName), "Searching introduction of: " + linter.linterName());
                Pair<RevCommit, String> introduction = linterIntroductionFinder.findIntroductionCommitWithProof(git, Linter.valueOf(linter.linterName()));
                lintersWithProof.add(new Proof.LinterWithProof(linter.linterName(), linter.fileName(), introduction.getKey().getName(), introduction.getValue(), introduction.getLeft().getCommitTime()));
            }

            Pair<Integer, Integer> startAndEndTime = getStartAndEndTime(lintersWithProof);

            return Optional.of(new Proof(
                            ownerName,
                            repoName,
                            Language.JAVA,
                            "https://github.com/" + ownerName + "/" + repoName,
                            currentBranch,
                            currentCommit.getName(),
                            currentCommit.getCommitTime(),
                            startAndEndTime.getLeft(),
                            startAndEndTime.getRight(),
                            lintersWithProof
                    )
            );
        }
    }

    private static Pair<Integer, Integer> getStartAndEndTime(List<Proof.LinterWithProof> lintersWithProof) {
        int startTime = Integer.MAX_VALUE;
        int endTime = Integer.MIN_VALUE;
        for (Proof.LinterWithProof linter : lintersWithProof) {
            int introductionTime = linter.introductionTime();

            int start = introductionTime - SECONDS_IN_TWO_YEARS;
            if (start < startTime) {
                startTime = start;
            }

            int end = introductionTime + SECONDS_IN_TWO_YEARS;
            if (end > endTime) {
                endTime = end;
            }
        }

        if (lintersWithProof.isEmpty()) {
            startTime = -1;
            endTime = -1;
        }
        return Pair.of(startTime, endTime);
    }

}
