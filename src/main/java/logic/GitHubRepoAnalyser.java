package logic;

import io.git.FullRepositoryDownloader;
import io.json.GitHubRepositoryReaderWriter;
import io.json.ProofReaderWriter;
import jakarta.inject.Inject;
import model.linting.Linter;
import model.writable.GitHubRepository;
import model.writable.Proof;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static util.Constants.SIZE_IN_BYTES;
import static util.Constants.SIZE_IN_KB;

public class GitHubRepoAnalyser {

    private final GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter;
    private final ProofReaderWriter proofReaderWriter;
    private final FullRepositoryDownloader fullRepositoryDownloader;

    private final LinterFinder linterFinder;
    private final LinterIntroductionFinder linterIntroductionFinder;

    private final StateHandler stateHandler;
    private final FilteredManager filteredManager;
    private final ExecutorService executorService;

    @Inject
    public GitHubRepoAnalyser(FullRepositoryDownloader fullRepositoryDownloader, LinterFinder linterFinder, LinterIntroductionFinder linterIntroductionFinder, ProofReaderWriter proofReaderWriter, StateHandler stateHandler, FilteredManager filteredManager, GitHubRepositoryReaderWriter gitHubRepositoryReaderWriter, ExecutorService executorService) {
        this.fullRepositoryDownloader = fullRepositoryDownloader;
        this.linterFinder = linterFinder;
        this.linterIntroductionFinder = linterIntroductionFinder;
        this.proofReaderWriter = proofReaderWriter;
        this.stateHandler = stateHandler;
        this.filteredManager = filteredManager;
        this.gitHubRepositoryReaderWriter = gitHubRepositoryReaderWriter;
        this.executorService = executorService;
    }

    public void startAnalysing() throws IOException {
        stateHandler.addToState(Pair.of("IO", ""), "Reading GitHub Project files");
        List<GitHubRepository> gitHubRepositories = gitHubRepositoryReaderWriter.readAll();
        gitHubRepositories.sort((o1, o2) -> o2.numberOfStars() - o1.numberOfStars());
        stateHandler.removeFromState(Pair.of("IO", ""));

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (GitHubRepository gitHubRepository : gitHubRepositories) {

            String repoOwner = gitHubRepository.repoOwner();
            String repoName = gitHubRepository.repoName();

            futures.add(CompletableFuture.runAsync(() -> {
                stateHandler.addToState(Pair.of(repoOwner, repoName), "Searching for linters");

                try {
                    handleRepo(repoOwner, repoName);
                } catch (Exception e) {
                    System.out.println("Error processing " + repoOwner + "/" + repoName);
                    e.printStackTrace();
                }

                stateHandler.removeFromState(Pair.of(repoOwner, repoName));
            }, executorService));
        }

        //Wait here, until loop is finished
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
    }

    private void handleRepo(String repoOwner, String repoName) throws GitAPIException, IOException {
        stateHandler.addToState(Pair.of(repoOwner, repoName), "Started handling");

        //Skip if already analysed
        if (proofReaderWriter.read(repoOwner, repoName).isPresent()) {
            return;
        }

        // Load cached data about GitHub repo
        GitHubRepository gitHubRepository = gitHubRepositoryReaderWriter.read(repoOwner, repoName).orElseThrow();

        //Size limit with the data from GitHub
        int size = gitHubRepository.size();
        if (size > SIZE_IN_KB) {
            filteredManager.filterCompleteProject(repoOwner, repoName, "Repo is too large in GitHub", String.valueOf(size));
            throw new RuntimeException("Repo too large!");
        }

        //Create and save proof
        Proof proof;
        try (Git git = fullRepositoryDownloader.getRepo(repoOwner, repoName, gitHubRepository.defaultBranch())) {
            proof = createProof(git, gitHubRepository);
        }
        proofReaderWriter.write(proof);
    }

    private Proof createProof(Git git, GitHubRepository gitHubRepository) throws GitAPIException, IOException {
        String repoOwner = gitHubRepository.repoOwner();
        String repoName = gitHubRepository.repoName();
        String defaultBranch = gitHubRepository.defaultBranch();
        int numberOfStars = gitHubRepository.numberOfStars();

        // Get commits
        File folder = git.getRepository().getDirectory().getParentFile();
        Iterator<RevCommit> commits = git.log().call().iterator();
        ArrayList<RevCommit> commitList = new ArrayList<>();
        commits.forEachRemaining(commitList::add);

        // Extract first and latest commit
        RevCommit currentCommit = commitList.getFirst();
        int currentCommitTime = currentCommit.getCommitTime();
        RevCommit firstCommit = commitList.getLast();
        String firstCommitName = firstCommit.getName();
        int firstCommitCommitTime = firstCommit.getCommitTime();

        //Limit size again here after download. The GitHub size is not very accurate.
        long directorySize = FileUtils.sizeOfDirectory(folder);
        if (directorySize > SIZE_IN_BYTES) {
            filteredManager.filterCompleteProject(repoOwner, repoName, "Repo is too large in file system", String.valueOf(directorySize));
            throw new RuntimeException("Repo too large!");
        }

        stateHandler.addToState(Pair.of(repoOwner, repoName), "Searching for linter");
        List<LinterFinder.Result> currentLinters = linterFinder.findLinters(folder);

        List<Proof.LinterWithProof> lintersWithProof = new ArrayList<>();
        for (LinterFinder.Result linter : currentLinters) {
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Searching introduction of: " + linter.linterName());
            Pair<RevCommit, String> introduction = linterIntroductionFinder.findIntroductionCommitWithProof(git, Linter.valueOf(linter.linterName()));

            // Only add linters, that are used longer then two years
            if (currentCommitTime - Constants.ANALYSIS_WINDOW_IN_SECONDS < introduction.getLeft().getCommitTime()) {
                filteredManager.filterProjectLinter(repoOwner, repoName, Linter.valueOf(linter.linterName()), "Linter did not exist for at least two years", String.valueOf(introduction));
            } else {
                lintersWithProof.add(new Proof.LinterWithProof(linter.linterName(), linter.fileName(), introduction.getKey().getName(), introduction.getValue(), introduction.getLeft().getCommitTime()));
            }
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

            int start = introductionTime - Constants.ANALYSIS_WINDOW_IN_SECONDS;
            if (start < startTime) {
                startTime = start;
            }

            int end = introductionTime + Constants.ANALYSIS_WINDOW_IN_SECONDS;
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
