package logic;

import io.git.BareRepositoryDownloader;
import io.json.CemProjectReaderWriter;
import io.json.ProofReaderWriter;
import jakarta.inject.Inject;
import model.CemProject;
import model.Proof;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GroupsCreator {

    private final ProofReaderWriter proofReaderWriter;
    private final CemMatcher cemMatcher;
    private final CemProjectReaderWriter cemProjectReaderWriter;
    private final BareRepositoryDownloader bareRepositoryDownloader;
    private final ExecutorService executorService;

    @Inject
    public GroupsCreator(ProofReaderWriter proofReaderWriter, CemMatcher cemMatcher, CemProjectReaderWriter cemProjectReaderWriter, BareRepositoryDownloader bareRepositoryDownloader, ExecutorService executorService) {
        this.proofReaderWriter = proofReaderWriter;
        this.cemMatcher = cemMatcher;
        this.cemProjectReaderWriter = cemProjectReaderWriter;
        this.bareRepositoryDownloader = bareRepositoryDownloader;
        this.executorService = executorService;
    }

    public void createTreatmentAndControlGroups() throws IOException {
        System.out.println("Started reading proof files");
        List<Proof> allProofs = proofReaderWriter.readAll();
        System.out.println("Finished reading proof files");
        System.out.println();

        // Group proofs into potential-treatment and potential-control
        System.out.println("Started converting");
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

        // Get final treatment-control-pairs
        System.out.println("Started matching");
        cemMatcher.matchAndSafe(treatedCandidates, controlCandidates);
    }

    private void createCemProjectsFiles(List<Proof> proofs, CemProject.GroupType groupType) {
        AtomicInteger counter = new AtomicInteger(0);
        CompletableFuture.allOf(proofs.stream().map(proof -> CompletableFuture.runAsync(() -> {
            try {
                if (cemProjectReaderWriter.getElement(proof.repoOwner(), proof.repoName()).isEmpty()) {
                    cemProjectReaderWriter.write(convertToCemProject(proof, groupType));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(groupType + ": " + counter.incrementAndGet() + "/" + proofs.size());
        }, executorService)).toList().toArray(new CompletableFuture[]{})).join();
    }

    private CemProject convertToCemProject(Proof proof, CemProject.GroupType groupType) {
        if (groupType == CemProject.GroupType.CONTROL && !proof.linters().isEmpty()) {
            System.err.println("Linters should be empty for control projects: " + proof.link());
        }

        if (groupType == CemProject.GroupType.TREATMENT && proof.linters().isEmpty()) {
            System.err.println("Linters should be non-empty for treatment projects: " + proof.link());
        }

        List<CemProject.Commit> commits = bareRepositoryDownloader.getCommits(proof.repoOwner(), proof.repoName(), proof.defaultBranch());
        return new CemProject(
                proof.repoOwner(),
                proof.repoName(),
                groupType,
                proof.numberOfStars(),
                String.valueOf(Instant.ofEpochSecond(proof.firstCommitTime()).atZone(ZoneId.of("UTC")).getYear()),
                proof.defaultBranch(),
                commits.size(),
                getTreatedLinterCommits(proof, commits),
                commits
        );
    }

    private static List<CemProject.Commit> getTreatedLinterCommits(Proof treatedProof, List<CemProject.Commit> commits) {
        Set<String> treatedLinterCommitNames = treatedProof.linters().stream().map(Proof.LinterWithProof::introductionCommit).collect(Collectors.toSet());
        List<CemProject.Commit> treatedLinterCommits = new ArrayList<>();
        for (CemProject.Commit treatedCommit : commits) {
            if (treatedLinterCommitNames.contains(treatedCommit.name())) {
                treatedLinterCommits.add(treatedCommit);
            }
        }
        return treatedLinterCommits;
    }

}
