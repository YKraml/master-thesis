import com.google.inject.Guice;
import com.google.inject.Injector;
import io.json.CemProjectPairReaderWriter;
import io.json.CemProjectReaderWriter;
import io.json.FilteredProjectReaderWriter;
import io.json.ProofReaderWriter;
import model.linting.Language;
import model.writable.CemProject;
import model.writable.CemProjectPair;
import model.writable.Proof;
import util.Constants;
import util.GuiceModule;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainStatistics {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new GuiceModule());
        ProofReaderWriter proofReaderWriter = injector.getInstance(ProofReaderWriter.class);
        FilteredProjectReaderWriter filteredProjectReaderWriter = injector.getInstance(FilteredProjectReaderWriter.class);
        CemProjectReaderWriter cemProjectReaderWriter = injector.getInstance(CemProjectReaderWriter.class);
        CemProjectPairReaderWriter cemProjectPairReaderWriter = injector.getInstance(CemProjectPairReaderWriter.class);

        outputMatchingResults(cemProjectReaderWriter, cemProjectPairReaderWriter, Language.JAVA);
        outputMatchingResults(cemProjectReaderWriter, cemProjectPairReaderWriter, Language.PYTHON);
        outputMatchingResults(cemProjectReaderWriter, cemProjectPairReaderWriter, Language.CPP);

        outputLinterUsage(proofReaderWriter, filteredProjectReaderWriter, Language.JAVA);
        outputLinterUsage(proofReaderWriter, filteredProjectReaderWriter, Language.PYTHON);
        outputLinterUsage(proofReaderWriter, filteredProjectReaderWriter, Language.CPP);

        outputExactLinterUsage(proofReaderWriter, Language.JAVA);
        outputExactLinterUsage(proofReaderWriter, Language.PYTHON);
        outputExactLinterUsage(proofReaderWriter, Language.CPP);

        outputExactLinterUsageCombinations(proofReaderWriter, Language.JAVA);
        outputExactLinterUsageCombinations(proofReaderWriter, Language.PYTHON);
        outputExactLinterUsageCombinations(proofReaderWriter, Language.CPP);
    }

    private static void outputMatchingResults(CemProjectReaderWriter cemProjectReaderWriter, CemProjectPairReaderWriter cemProjectPairReaderWriter, Language language) throws IOException {
        Constants.CURRENT_LANGUAGE = language;
        List<CemProject> cemProjects = cemProjectReaderWriter.readAll();
        List<CemProjectPair> cemProjectPairs = cemProjectPairReaderWriter.readAll();

        int potentialControl = 0;
        int potentialTreatment = 0;

        for (CemProject cemProject : cemProjects) {
            if (cemProject.groupType().equals(CemProject.GroupType.CONTROL)) {
                potentialControl++;
            } else {
                potentialTreatment++;
            }
        }

        System.out.println();
        System.out.println(language);
        System.out.println("Potential Control: " + potentialControl);
        System.out.println("Potential Treatment: " + potentialTreatment);
        System.out.println("Final Group Sizes: " + cemProjectPairs.size());
    }

    private static void outputExactLinterUsageCombinations(ProofReaderWriter proofReaderWriter, Language language) throws IOException {
        Constants.CURRENT_LANGUAGE = language;
        List<Proof> proofs = proofReaderWriter.readAll();
        Map<String, Integer> combinationMap = new HashMap<>();

        for (Proof proof : proofs) {
            List<String> currentLinters = proof.linters().stream()
                    .map(Proof.LinterWithProof::linterName)
                    .distinct()
                    .sorted()
                    .toList();

            if (!currentLinters.isEmpty()) {
                String combinationKey = String.join(" & ", currentLinters);
                combinationMap.merge(combinationKey, 1, Integer::sum);
            }
        }

        System.out.println();
        System.out.println("Language: " + language);
        combinationMap.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    private static void outputExactLinterUsage(ProofReaderWriter proofReaderWriter, Language language) throws IOException {
        Constants.CURRENT_LANGUAGE = language;
        List<Proof> proofs = proofReaderWriter.readAll();
        Map<String, Integer> linterIntegerMap = new HashMap<>();
        for (Proof proof : proofs) {
            for (Proof.LinterWithProof linter : proof.linters()) {
                String linterName = linter.linterName();
                linterIntegerMap.compute(linterName, (k, oldValue) -> {
                    if (oldValue == null) {
                        return 1;
                    }
                    return oldValue + 1;
                });
                break;
            }
        }

        System.out.println();
        System.out.println(language);
        System.out.println(linterIntegerMap);
    }

    private static void outputLinterUsage(ProofReaderWriter proofReaderWriter, FilteredProjectReaderWriter filteredProjectReaderWriter, Language language) throws IOException {
        Constants.CURRENT_LANGUAGE = language;
        List<Proof> proofs = proofReaderWriter.readAll();
        int tooLarge = filteredProjectReaderWriter.readAll().size();

        int projectsWithLinter = 0;
        int projectsWithoutLiner = 0;
        for (Proof proof : proofs) {
            if (proof.linters().isEmpty()) {
                projectsWithoutLiner++;
            } else {
                projectsWithLinter++;
            }
        }

        int rest = 20000 - projectsWithLinter - projectsWithoutLiner - tooLarge;

        System.out.println();
        System.out.println(language);
        System.out.println("Projects with linter: " + projectsWithLinter);
        System.out.println("Projects without linter: " + projectsWithoutLiner);
        System.out.println("Projects too large: " + tooLarge);
        System.out.println("Error: " + rest);
    }
}
