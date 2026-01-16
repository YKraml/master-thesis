package logic;

import io.git.BareRepositoryDownloader;
import io.json.CemProjectPairReaderWriter;
import io.json.ProofReaderWriter;
import jakarta.inject.Inject;
import model.CemProject;
import model.Proof;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsCreator {

    private final ProofReaderWriter proofReaderWriter;
    private final CemProjectPairReaderWriter cemProjectPairReaderWriter;
    private final CemMatcher cemMatcher;
    private final BareRepositoryDownloader bareRepositoryDownloader;

    @Inject
    public GroupsCreator(ProofReaderWriter proofReaderWriter, CemProjectPairReaderWriter cemProjectPairReaderWriter, CemMatcher cemMatcher, BareRepositoryDownloader bareRepositoryDownloader) {
        this.proofReaderWriter = proofReaderWriter;
        this.cemProjectPairReaderWriter = cemProjectPairReaderWriter;
        this.cemMatcher = cemMatcher;
        this.bareRepositoryDownloader = bareRepositoryDownloader;
    }

    public void createTreatmentAndControlGroups() throws IOException {
        System.out.println("Started reading proof files");
        List<Proof> allProofs = proofReaderWriter.readAll();
        System.out.println("Finished reading proof files");
        System.out.println();

        // Group proofs into potential-treatment and potential-control
        System.out.println("Started converting");
        Map<CemProject, Proof> treatedCandidates = new HashMap<>();
        Map<CemProject, Proof> controlCandidates = new HashMap<>();
        allProofs.forEach(proof -> {
            if (proof.linters().isEmpty()) {
                controlCandidates.put(convertToCemProject(proof, CemProject.GroupType.CONTROL), proof);
            } else {
                treatedCandidates.put(convertToCemProject(proof, CemProject.GroupType.TREATMENT), proof);
            }
        });

        // Get final treatment-control-pairs
        System.out.println("Started matching");
        cemMatcher.matchAndSafe(treatedCandidates, controlCandidates);
    }

    private CemProject convertToCemProject(Proof proof, CemProject.GroupType groupType) {
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
                String.valueOf(Instant.ofEpochSecond(proof.firstCommitTime()).atZone(ZoneId.of("UTC")).getYear()),
                proof.defaultBranch(),
                -1,
                null
        );
    }

}
