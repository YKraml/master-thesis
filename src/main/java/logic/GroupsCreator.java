package logic;

import io.git.FullRepositoryDownloader;
import io.json.CemProjectReaderWriter;
import io.json.ProofReaderWriter;
import io.scc.SccExecutor;
import io.scc.SccObject;
import jakarta.inject.Inject;
import model.CemProject;
import model.Proof;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jgit.api.errors.GitAPIException;
import util.CemCommitsUtil;
import util.Constants;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class GroupsCreator {

    private final ProofReaderWriter proofReaderWriter;
    private final CemProjectReaderWriter cemProjectReaderWriter;
    private final CemMatcher cemMatcher;

    private final FullRepositoryDownloader fullRepositoryDownloader;

    private final ExecutorService executorService;
    private final CemCommitsUtil cemCommitsUtil;
    private final SccExecutor sccExecutor;
    private final StateHandler stateHandler;

    @Inject
    public GroupsCreator(ProofReaderWriter proofReaderWriter, CemMatcher cemMatcher, CemProjectReaderWriter cemProjectReaderWriter, FullRepositoryDownloader fullRepositoryDownloader, ExecutorService executorService, CemCommitsUtil cemCommitsUtil, SccExecutor sccExecutor, StateHandler stateHandler) {
        this.proofReaderWriter = proofReaderWriter;
        this.cemMatcher = cemMatcher;
        this.cemProjectReaderWriter = cemProjectReaderWriter;
        this.fullRepositoryDownloader = fullRepositoryDownloader;
        this.executorService = executorService;
        this.cemCommitsUtil = cemCommitsUtil;
        this.sccExecutor = sccExecutor;
        this.stateHandler = stateHandler;
    }

    public void createTreatmentAndControlGroups() throws IOException {
        System.out.println("Started reading proof files");
        List<Proof> allProofs = proofReaderWriter.readAll();
        System.out.println("Finished reading proof files");
        System.out.println();

        stateHandler.start();

        // Group proofs into potential-treatment and potential-control
        List<Proof> treatedCandidates = new ArrayList<>();
        List<Proof> controlCandidates = new ArrayList<>();
        allProofs.forEach(proof -> {
            if (proof.linters().isEmpty()) {
                controlCandidates.add(proof);
            } else {
                treatedCandidates.add(proof);
            }
        });

        // Create CemProjects for caching
        createCemProjectsFiles(treatedCandidates, CemProject.GroupType.TREATMENT);
        createCemProjectsFiles(controlCandidates, CemProject.GroupType.CONTROL);

        stateHandler.end();

        // Get final treatment-control-pairs
        System.out.println("Started matching");
        cemMatcher.matchAndSafe(treatedCandidates, controlCandidates);
    }

    private void createCemProjectsFiles(List<Proof> proofs, CemProject.GroupType groupType) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Proof proof : proofs) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                stateHandler.addToState(Pair.of(proof.repoOwner(), proof.repoName()), "Converting to cem project");
                try {
                    if (cemProjectReaderWriter.getElement(proof.repoOwner(), proof.repoName()).isEmpty()) {
                        cemProjectReaderWriter.write(convertToCemProject(proof, groupType));
                    }
                } catch (IOException | GitAPIException e) {
                    throw new RuntimeException(e);
                } finally {
                    stateHandler.removeFromState(Pair.of(proof.repoOwner(), proof.repoName()));
                }
            }, executorService);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
    }

    private CemProject convertToCemProject(Proof proof, CemProject.GroupType groupType) throws GitAPIException, IOException {
        if (groupType == CemProject.GroupType.CONTROL && !proof.linters().isEmpty()) {
            System.err.println("Linters should be empty for control projects: " + proof.link());
        }

        if (groupType == CemProject.GroupType.TREATMENT && proof.linters().isEmpty()) {
            System.err.println("Linters should be non-empty for treatment projects: " + proof.link());
        }

        String repoOwner = proof.repoOwner();
        String repoName = proof.repoName();

        // Fetch commits
        stateHandler.addToState(Pair.of(proof.repoOwner(), proof.repoName()), "Getting commits");
        List<CemProject.Commit> commits = fullRepositoryDownloader.getCommits(repoOwner, repoName, proof.defaultBranch());

        //Count lines
        stateHandler.addToState(Pair.of(proof.repoOwner(), proof.repoName()), "Counting lines");
        List<SccObject> sccObjects = sccExecutor.execute(fullRepositoryDownloader.getFolderName(repoOwner, repoName));
        int loc = sccObjects.stream().filter(sccObject -> Constants.CURRENT_LANGUAGE.getSccFileEndings().contains(sccObject.Name())).mapToInt(SccObject::Lines).sum();
        int sloc = sccObjects.stream().filter(sccObject -> Constants.CURRENT_LANGUAGE.getSccFileEndings().contains(sccObject.Name())).mapToInt(SccObject::Code).sum();

        //Count contributors
        int numberOfContributors = Math.toIntExact(commits.stream().map(CemProject.Commit::user).distinct().count());

        return new CemProject(
                repoOwner,
                repoName,
                groupType,
                proof.numberOfStars(),
                String.valueOf(Instant.ofEpochSecond(proof.firstCommitTime()).atZone(ZoneId.of("UTC")).getYear()),
                proof.defaultBranch(),
                commits.size(),
                loc,
                sloc,
                numberOfContributors,
                getTreatedLinterCommits(proof, commits),
                commits
        );
    }

    private List<CemProject.LinterCommit> getTreatedLinterCommits(Proof treatedProof, List<CemProject.Commit> commits) {
        Map<String, String> treatedLinterCommitNames = new HashMap<>();
        for (Proof.LinterWithProof linterWithProof : treatedProof.linters()) {
            String introductionCommit = linterWithProof.introductionCommit();
            treatedLinterCommitNames.put(introductionCommit, linterWithProof.linterName());
        }

        List<CemProject.LinterCommit> treatedLinterCommits = new ArrayList<>();
        for (CemProject.Commit treatedCommit : commits) {
            String commitName = treatedCommit.name();
            if (treatedLinterCommitNames.containsKey(commitName)) {

                int commitTime = treatedCommit.time();
                int commitIndex = treatedCommit.index();
                String linterName = treatedLinterCommitNames.get(commitName);
                int numberOfCommitsInAnalysisWindow = cemCommitsUtil.calcNumberOfCommitsInAnalysisWindow(commits, treatedCommit);
                String user = treatedCommit.user();

                treatedLinterCommits.add(new CemProject.LinterCommit(commitName, commitTime, commitIndex, linterName, user, numberOfCommitsInAnalysisWindow));
            }
        }
        return treatedLinterCommits;
    }
}
