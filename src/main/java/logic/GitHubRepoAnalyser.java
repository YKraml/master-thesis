package logic;

import io.git.FullRepositoryDownloader;
import io.json.GitHubRepositoryReaderWriter;
import io.json.ProofReaderWriter;
import jakarta.inject.Inject;
import model.GitHubRepository;
import model.Linter;
import model.Proof;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static util.Constants.SIZE_1;
import static util.Constants.SIZE_2;

public class GitHubRepoAnalyser {

    private final FullRepositoryDownloader fullRepositoryDownloader;
    private final LinterFinder linterFinder;
    private final LinterIntroductionFinder linterIntroductionFinder;
    private final ProofReaderWriter proofReaderWriter;
    private final StateHandler stateHandler;
    private final FilteredManager filteredManager;
    private final GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter;

    @Inject
    public GitHubRepoAnalyser(FullRepositoryDownloader fullRepositoryDownloader, LinterFinder linterFinder, LinterIntroductionFinder linterIntroductionFinder, ProofReaderWriter proofReaderWriter, StateHandler stateHandler, FilteredManager filteredManager, GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter) {
        this.fullRepositoryDownloader = fullRepositoryDownloader;
        this.linterFinder = linterFinder;
        this.linterIntroductionFinder = linterIntroductionFinder;
        this.proofReaderWriter = proofReaderWriter;
        this.stateHandler = stateHandler;
        this.filteredManager = filteredManager;
        this.gitHubRepositoryReaderWriter = gitHubRepositoryReaderWriter;
    }

    public Proof handleRepo(String repoOwner, String repoName) throws GitAPIException, IOException {
        stateHandler.addToState(Pair.of(repoOwner, repoName), "Started handling");

        //Skip if already analysed
        Optional<Proof> cachedProof = proofReaderWriter.getElement(repoOwner, repoName);
        if (cachedProof.isPresent()) {
            return cachedProof.get();
        }

        //Search in repo for linters
        Proof proof = searchForProof(repoOwner, repoName);
        proofReaderWriter.write(proof);
        return proof;
    }

    private Proof searchForProof(String repoOwner, String repoName) throws IOException, GitAPIException {
        GitHubRepository gitHubRepository = gitHubRepositoryReaderWriter.getElement(repoOwner, repoName).orElseThrow();
        String defaultBranch = gitHubRepository.defaultBranch();
        int numberOfStars = gitHubRepository.numberOfStars();
        int size = gitHubRepository.size();

        //Size limit with the data from GitHub
        if (size > SIZE_1) {
            filteredManager.filterCompleteProject(repoOwner, repoName, "Repo is too large in GitHub", String.valueOf(size));
            throw new RuntimeException("Repo too large!");
        }

        try (Git git = fullRepositoryDownloader.getRepo(repoOwner, repoName, defaultBranch)) {
            return searchForProof(repoOwner, repoName, git, defaultBranch, numberOfStars);
        }
    }

    private Proof searchForProof(String repoOwner, String repoName, Git git, String defaultBranch, int numberOfStars) throws GitAPIException, IOException {
        File folder = git.getRepository().getDirectory().getParentFile();
        Iterator<RevCommit> commits = git.log().call().iterator();
        ArrayList<RevCommit> commitList = new ArrayList<>();
        commits.forEachRemaining(commitList::add);

        RevCommit currentCommit = commitList.getFirst();
        int currentCommitTime = currentCommit.getCommitTime();
        RevCommit firstCommit = commitList.getLast();
        String firstCommitName = firstCommit.getName();
        int firstCommitCommitTime = firstCommit.getCommitTime();

        if (firstCommitCommitTime < 0) {
            throw new RuntimeException("First commit time is negative");
        }

        //Limit again here after download. The GitHub size is not very accurate.
        long directorySize = FileUtils.sizeOfDirectory(folder);
        if (directorySize > SIZE_2) {
            filteredManager.filterCompleteProject(repoOwner, repoName, "Repo is too large in file system", String.valueOf(directorySize));
            throw new RuntimeException("Repo too large!");
        }

        stateHandler.addToState(Pair.of(repoOwner, repoName), "Searching for linter");
        List<LinterFinder.Result> currentLinters = linterFinder.findLinters(folder);

        List<Proof.LinterWithProof> lintersWithProof = new ArrayList<>();

        for (LinterFinder.Result linter : currentLinters) {
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Searching introduction of: " + linter.linterName());
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
                defaultBranch,
                currentCommit.getName(),
                currentCommitTime,
                firstCommitName,
                firstCommitCommitTime,
                startAndEndTime.getLeft(),
                startAndEndTime.getRight(),
                numberOfStars,
                lintersWithProof
        );
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
}
