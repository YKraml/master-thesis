package logic;

import io.json.CemProjectPairReaderWriter;
import io.json.ProofReaderWriter;
import jakarta.inject.Inject;
import model.CemProject;
import model.CemProjectPair;
import model.Proof;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GroupsCreator {

    private final ProofReaderWriter proofReaderWriter;
    private final CemProjectPairReaderWriter cemProjectPairReaderWriter;
    private final CemMatcher cemMatcher;

    @Inject
    public GroupsCreator(ProofReaderWriter proofReaderWriter, CemProjectPairReaderWriter cemProjectPairReaderWriter, CemMatcher cemMatcher) {
        this.proofReaderWriter = proofReaderWriter;
        this.cemProjectPairReaderWriter = cemProjectPairReaderWriter;
        this.cemMatcher = cemMatcher;
    }

    public void createTreatmentAndControlGroups() throws IOException {
        System.out.println("Started reading proof files");
        List<Proof> allProofs = proofReaderWriter.readAll();
        System.out.println("Finished reading proof files");
        System.out.println();

        // Group proofs into potential-treatment and potential-control
        List<Proof> potentialTreatmentCandidates = new ArrayList<>();
        List<Proof> potentialControlCandidates = new ArrayList<>();
        for (Proof proof : allProofs) {
            if (proof.linters().isEmpty()) {
                potentialControlCandidates.add(proof);
            } else {
                potentialTreatmentCandidates.add(proof);
            }
        }

        // Get final-treatment and final-control groups
        List<CemProject> allTreatmentProjects = potentialTreatmentCandidates
                .stream()
                .map(proof -> convertToCemProject(proof, CemProject.GroupType.TREATMENT))
                .toList();
        List<CemProject> allControlProjects = potentialControlCandidates
                .stream()
                .map(proof -> convertToCemProject(proof, CemProject.GroupType.CONTROL))
                .collect(Collectors.toList());

        List<CemProjectPair> pairs = cemMatcher
                .match(allTreatmentProjects, allControlProjects)
                .stream()
                .sorted((o1, o2) -> o2.treated().numberOfStars() - o1.treated().numberOfStars())
                .toList();

        cemProjectPairReaderWriter.deleteFolder();
        for (CemProjectPair pair : pairs) {
            cemProjectPairReaderWriter.write(pair);
        }
    }

    private static CemProject convertToCemProject(Proof proof, CemProject.GroupType groupType) {

        if (groupType == CemProject.GroupType.CONTROL && !proof.linters().isEmpty()) {
            System.err.println("Linters should be empty for control projects: " + proof.link());
        }

        if (groupType == CemProject.GroupType.TREATMENT && proof.linters().isEmpty()) {
            System.err.println("Linters should be non-empty for treatment projects: " + proof.link());
        }

        return new CemProject(
                proof.repoOwner(),
                proof.repoName(),
                groupType,
                proof.numberOfStars(),
                proof.analysisStartTime(),
                proof.analysisEndTime(),
                String.valueOf(Instant.ofEpochSecond(proof.firstCommitTime()).atZone(ZoneId.of("UTC")).getYear()),
                proof.defaultBranch()
        );
    }
}
