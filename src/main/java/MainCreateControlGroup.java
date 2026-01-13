import com.google.inject.Guice;
import com.google.inject.Injector;
import io.json.CemProjectReaderWriter;
import io.json.CombinedDataReaderWriter;
import io.json.ProofReaderWriter;
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

        System.out.println("Reading files");
        List<Proof> treatmentGroup = combinedDataReaderWriter.readAll().stream().map(CombinedData::proof).toList();
        List<Proof> controlGroupCandidates = proofReaderWriter.readAll().stream().filter(proof -> proof.linters().isEmpty()).toList();

        System.out.println("Treatment group size: " + treatmentGroup.size());
        System.out.println("Control group size: " + controlGroupCandidates.size());

        System.out.println("Writing CEM Projects");
        for (Proof proof : treatmentGroup) {
            List<CemProject> cemProjects = convertToCemProjects(proof);

            for (CemProject cemProject : cemProjects) {
                cemProjectReaderWriter.write(cemProject);
            }
        }

    }

    private static List<CemProject> convertToCemProjects(Proof proof) {
        List<CemProject> cemProjects = new ArrayList<>();

        if (proof.linters().isEmpty()) {
            System.err.println("No linters in project: " + proof.link());
        }

        for (Proof.LinterWithProof linterWithProof : proof.linters()) {
            String repoOwner = proof.repoOwner();
            String repoName = proof.repoName();
            String id = repoOwner + "." + repoName + "." + linterWithProof.linterName();
            CemProject.GroupType groupType = CemProject.GroupType.TREATMENT;
            int numberOfStars = proof.numberOfStars();
            double ageAtLinterIntro = (linterWithProof.introductionTime() - proof.firstCommitTime()) / 60d / 60d / 24d / 365d;
            String creationYear = String.valueOf(Instant.ofEpochSecond(proof.firstCommitTime()).atZone(ZoneId.of("UTC")).getYear());
            CemProject cemProject = new CemProject(id, groupType, numberOfStars, ageAtLinterIntro, creationYear);
            cemProjects.add(cemProject);
        }

        return cemProjects;
    }

}
