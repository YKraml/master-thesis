package logic;

import io.ProofReaderWriter;
import io.RepositoryDownloader;
import jakarta.inject.Inject;
import model.Linter;
import model.Proof;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class RepositoryHandler {

    private static final int SIZE_1 = 1000 * 1000; // 1 GB
    private static final int SIZE_2 = 1000 * 1000 * 1000; // 1 GB

    private final RepositoryDownloader repositoryDownloader;
    private final LinterFinder linterFinder;
    private final LinterIntroductionFinder linterIntroductionFinder;
    private final ProofReaderWriter proofReaderWriter;
    private final State state;
    private final GitHub github;
    private final FilteredManager filteredManager;

    @Inject
    public RepositoryHandler(RepositoryDownloader repositoryDownloader, LinterFinder linterFinder, LinterIntroductionFinder linterIntroductionFinder, ProofReaderWriter proofReaderWriter, State state, GitHub github, FilteredManager filteredManager) {
        this.repositoryDownloader = repositoryDownloader;
        this.linterFinder = linterFinder;
        this.linterIntroductionFinder = linterIntroductionFinder;
        this.proofReaderWriter = proofReaderWriter;
        this.state = state;
        this.github = github;
        this.filteredManager = filteredManager;
    }

    private static Pair<Integer, Integer> getStartAndEndTime(List<Proof.LinterWithProof> lintersWithProof) {
        int startTime = Integer.MAX_VALUE;
        int endTime = Integer.MIN_VALUE;
        for (Proof.LinterWithProof linter : lintersWithProof) {
            int introductionTime = linter.introductionTime();

            int start = introductionTime - Constants.SECONDS_IN_TWO_YEARS;
            if (start < startTime) {
                startTime = start;
            }

            int end = introductionTime + Constants.SECONDS_IN_TWO_YEARS;
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

    public void handleRepo(GHRepository repo) throws Exception {
        String repoOwner = repo.getOwnerName();
        String repoName = repo.getName();
        state.addToState(Pair.of(repoOwner, repoName), "Started handling");

        //Size limit with the data from GitHub
        if (repo.getSize() > SIZE_1) {
            filteredManager.filterCompleteProject(repoOwner, repoName, "Repo is too large in GitHub", String.valueOf(repo.getSize()));
            return;
        }

        handleRepo(repoOwner, repoName);
    }

    public Proof handleRepo(String ownerName, String repoName) throws GitAPIException, IOException {
        Proof proof = searchForProof(ownerName, repoName);
        proofReaderWriter.write(proof);
        return proof;
    }

    private Proof searchForProof(String repoOwner, String repoName) throws IOException, GitAPIException {
        //Skip if already analysed

        Optional<Proof> proof = proofReaderWriter.getElement(repoOwner, repoName);
        if (proof.isPresent()) {
            return proof.get();
        }

        int numberOfStars = github.getRepository(repoOwner + "/" + repoName).getStargazersCount();

        try (Git git = repositoryDownloader.getRepo(repoOwner, repoName)) {
            File folder = git.getRepository().getDirectory().getParentFile();
            Iterator<RevCommit> commits = git.log().call().iterator();
            ArrayList<RevCommit> commitList = new ArrayList<>();
            commits.forEachRemaining(commitList::add);

            RevCommit currentCommit = commitList.getFirst();
            int currentCommitTime = currentCommit.getCommitTime();
            RevCommit firstCommit = commitList.getLast();
            String currentBranch = git.getRepository().getBranch();

            //Limit again here after download. The getSize() is not very accurate.
            long size = FileUtils.sizeOfDirectory(folder);
            if (size > SIZE_2) {
                filteredManager.filterCompleteProject(repoOwner, repoName, "Local Repo is too large", String.valueOf(size));
                return new Proof(
                        repoOwner,
                        repoName,
                        Constants.CURRENT_LANGUAGE,
                        "https://github.com/" + repoOwner + "/" + repoName,
                        currentBranch,
                        currentCommit.getName(),
                        currentCommitTime,
                        firstCommit.getName(),
                        firstCommit.getCommitTime(),
                        -1,
                        -1,
                        numberOfStars,
                        List.of()
                );
            }

            state.addToState(Pair.of(repoOwner, repoName), "Searching for linter");
            List<LinterFinder.Result> currentLinters = linterFinder.findLinters(folder);

            List<Proof.LinterWithProof> lintersWithProof = new ArrayList<>();

            for (LinterFinder.Result linter : currentLinters) {
                state.addToState(Pair.of(repoOwner, repoName), "Searching introduction of: " + linter.linterName());
                Pair<RevCommit, String> introduction = linterIntroductionFinder.findIntroductionCommitWithProof(git, Linter.valueOf(linter.linterName()));

                if (currentCommitTime - Constants.SECONDS_IN_TWO_YEARS < introduction.getLeft().getCommitTime()) {
                    filteredManager.filterProjectLinter(repoOwner, repoName, Linter.valueOf(linter.linterName()), "Linter did not exist for at least two years", String.valueOf(introduction));
                    continue;
                }

                lintersWithProof.add(new Proof.LinterWithProof(linter.linterName(), linter.fileName(), introduction.getKey().getName(), introduction.getValue(), introduction.getLeft().getCommitTime()));
            }

            Pair<Integer, Integer> startAndEndTime = getStartAndEndTime(lintersWithProof);

            return new Proof(
                    repoOwner,
                    repoName,
                    Constants.CURRENT_LANGUAGE,
                    "https://github.com/" + repoOwner + "/" + repoName,
                    currentBranch,
                    currentCommit.getName(),
                    currentCommitTime,
                    firstCommit.getName(),
                    firstCommit.getCommitTime(),
                    startAndEndTime.getLeft(),
                    startAndEndTime.getRight(),
                    numberOfStars,
                    lintersWithProof
            );
        }
    }
}
