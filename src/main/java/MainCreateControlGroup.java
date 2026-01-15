import com.google.inject.Guice;
import com.google.inject.Injector;
import io.json.CemProjectReaderWriter;
import io.json.CombinedDataReaderWriter;
import io.json.ProofReaderWriter;
import logic.CemMatcher;
import model.CemProject;
import model.CombinedData;
import model.Proof;
import util.GuiceModule;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class MainCreateControlGroup {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        CombinedDataReaderWriter combinedDataReaderWriter = injector.getInstance(CombinedDataReaderWriter.class);
        ProofReaderWriter proofReaderWriter = injector.getInstance(ProofReaderWriter.class);
        CemProjectReaderWriter cemProjectReaderWriter = injector.getInstance(CemProjectReaderWriter.class);
        CemMatcher cemMatcher = injector.getInstance(CemMatcher.class);

        System.out.println("Reading files");
        List<Proof> treatmentGroup = combinedDataReaderWriter.readAll().stream().map(CombinedData::proof).toList();
        List<Proof> controlGroupCandidates = proofReaderWriter.readAll().stream().filter(proof -> proof.linters().isEmpty()).toList();

        System.out.println("Treatment group size: " + treatmentGroup.size());
        System.out.println("Control group size: " + controlGroupCandidates.size());

        List<CemProject> treatedProjects = new ArrayList<>();
        for (Proof proof : treatmentGroup) {
            treatedProjects.add(convertToCemProjects(proof, CemProject.GroupType.TREATMENT));
        }

        List<CemProject> candidatePool = new ArrayList<>();
        for (Proof proof : controlGroupCandidates) {
            candidatePool.add(convertToCemProjects(proof, CemProject.GroupType.CONTROL));
        }

        List<CemProject> finalControlGroup = cemMatcher.match(treatedProjects, candidatePool);

        System.out.println("Finished");

        // System.out.println("Writing CEM Projects");
        // for (Proof proof : treatmentGroup) {
        //     List<CemProject> cemProjects = convertToCemProjects(proof, CemProject.GroupType.CONTROL);
        //     for (CemProject cemProject : cemProjects) {
        //         cemProjectReaderWriter.write(cemProject);
        //     }
        // }
    }

    private static CemProject convertToCemProjects(Proof proof, CemProject.GroupType groupType) {

        if (groupType == CemProject.GroupType.CONTROL && !proof.linters().isEmpty()) {
            System.err.println("Linters should be empty for control projects: " + proof.link());
        }

        if (groupType == CemProject.GroupType.TREATMENT && proof.linters().isEmpty()) {
            System.err.println("Linters should be non-empty for treatment projects: " + proof.link());
        }

        List<Double> ageAtLinterIntros = new ArrayList<>();
        for (Proof.LinterWithProof linterWithProof : proof.linters()) {
            double ageAtLinterIntro = (linterWithProof.introductionTime() - proof.firstCommitTime()) / 60d / 60d / 24d / 365d;
            ageAtLinterIntros.add(ageAtLinterIntro);
        }

        String repoOwner = proof.repoOwner();
        String repoName = proof.repoName();

        int numberOfStars = proof.numberOfStars();

        String creationYear = String.valueOf(Instant.ofEpochSecond(proof.firstCommitTime()).atZone(ZoneId.of("UTC")).getYear());

        return new CemProject(repoOwner, repoName, groupType, numberOfStars, ageAtLinterIntros, creationYear);
    }
}
