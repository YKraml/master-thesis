package logic;

import com.google.inject.Inject;
import io.teamscale.api.model.churn.Finding;
import io.teamscale.api.model.churn.FindingChurn;
import io.teamscale.api.model.cleaned.CleanedMetrics;
import io.teamscale.api.model.cleaned.TeamscaleData;
import io.teamscale.api.model.cleaned.TeamscaleDataEntry;
import model.CemProject;
import model.CombinedData;
import model.Proof;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class DataCombiner {

    private final StateHandler stateHandler;

    @Inject
    public DataCombiner(StateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    public CombinedData combineData(Proof proof, TeamscaleData data, CemProject cemProject) {
        String repoOwner = data.repoOwner();
        String repoName = data.repoName();

        return new CombinedData(repoOwner, repoName, proof, cemProject, createEntriesFromData(repoOwner, repoName, data));
    }

    private List<CombinedData.Entry> createEntriesFromData(String repoOwner, String repoName, TeamscaleData teamscaleData) {
        List<CombinedData.Entry> resultEntries = new ArrayList<>();
        List<TeamscaleDataEntry> dataEntries = teamscaleData.entries();

        CombinedData.Entry beforeEntry = CombinedData.Entry.getDefault();
        for (int i = 0; i < dataEntries.size(); i++) {
            TeamscaleDataEntry datum = dataEntries.get(i);
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Converting entry from commit " + i + "/" + dataEntries.size());
            CombinedData.Entry entryFromData = createEntryFromData(datum.getFindingChurn(), datum.getMetrics(), beforeEntry);
            beforeEntry = entryFromData;
            resultEntries.add(entryFromData);
        }

        return resultEntries;
    }

    private CombinedData.Entry createEntryFromData(FindingChurn findingChurn, CleanedMetrics cleanedMetrics, CombinedData.Entry beforeEntry) {
        int time = Math.toIntExact(cleanedMetrics.timestamp());
        int files = cleanedMetrics.files();
        int linesOfCode = cleanedMetrics.linesOfCode();
        int sourceLinesOfCode = cleanedMetrics.sourceLinesOfCode();
        int findings = (int) cleanedMetrics.numberOfFindings();
        double cloneCoverage = cleanedMetrics.cloneCoverage();

        // Finding categories
        int comprehensibility = beforeEntry.comprehensibility();
        int correctness = beforeEntry.correctness();
        int documentation = beforeEntry.documentation();
        int efficiency = beforeEntry.efficiency();
        int errorHandling = beforeEntry.errorHandling();
        int redundancy = beforeEntry.redundancy();
        int structure = beforeEntry.structure();
        int security = beforeEntry.security();
        for (Finding finding : findingChurn.addedFindings()) {
            switch (finding.categoryName()) {
                case "Comprehensibility" -> comprehensibility++;
                case "Correctness" -> correctness++;
                case "Documentation" -> documentation++;
                case "Efficiency" -> efficiency++;
                case "Error Handling" -> errorHandling++;
                case "Redundancy" -> redundancy++;
                case "Structure" -> structure++;
                case "Security" -> security++;
                default -> throw new IllegalStateException("Unexpected value: " + finding.categoryName());
            }
        }
        for (Finding finding : findingChurn.removedFindings()) {
            switch (finding.categoryName()) {
                case "Comprehensibility" -> comprehensibility--;
                case "Correctness" -> correctness--;
                case "Documentation" -> documentation--;
                case "Efficiency" -> efficiency--;
                case "Error Handling" -> errorHandling--;
                case "Redundancy" -> redundancy--;
                case "Structure" -> structure--;
                case "Security" -> security--;
                default -> throw new IllegalStateException("Unexpected value: " + finding.categoryName());
            }
        }
        double comprehensibilityDensity = (double) comprehensibility / sourceLinesOfCode * 1000;
        double correctnessDensity = (double) correctness / sourceLinesOfCode * 1000;
        double documentationDensity = (double) documentation / sourceLinesOfCode * 1000;
        double efficiencyDensity = (double) efficiency / sourceLinesOfCode * 1000;
        double errorHandlingDensity = (double) errorHandling / sourceLinesOfCode * 1000;
        double redundancyDensity = (double) redundancy / sourceLinesOfCode * 1000;
        double structureDensity = (double) structure / sourceLinesOfCode * 1000;
        double securityDensity = (double) security / sourceLinesOfCode * 1000;

        // only structure category
        int longFiles = beforeEntry.longFiles();
        int longMethods = beforeEntry.longMethods();
        int deepNesting = beforeEntry.deepNesting();
        for (Finding finding : findingChurn.addedFindings()) {
            switch (finding.groupName()) {
                case "File Size" -> longFiles++;
                case "Method Length" -> longMethods++;
                case "Nesting Depth" -> deepNesting++;
            }
        }
        for (Finding finding : findingChurn.removedFindings()) {
            switch (finding.groupName()) {
                case "File Size" -> longFiles--;
                case "Method Length" -> longMethods--;
                case "Nesting Depth" -> deepNesting--;
            }
        }

        double longFilesDensity = (double) longFiles / sourceLinesOfCode * 1000;
        double longMethodDensity = (double) longMethods / sourceLinesOfCode * 1000;
        double deepNestingDensity = (double) deepNesting / sourceLinesOfCode * 1000;
        double findingsDensity = (double) findings / sourceLinesOfCode * 1000;
        double averageFileLoc = (double) sourceLinesOfCode / files;
        double averageFileSloc = (double) linesOfCode / files;

        return new CombinedData.Entry(
                time,
                files,
                linesOfCode,
                sourceLinesOfCode,
                longFiles,
                longMethods,
                deepNesting,
                findings,
                comprehensibility,
                correctness,
                documentation,
                efficiency,
                errorHandling,
                redundancy,
                structure,
                security,
                comprehensibilityDensity,
                correctnessDensity,
                documentationDensity,
                efficiencyDensity,
                errorHandlingDensity,
                redundancyDensity,
                structureDensity,
                securityDensity,
                longFilesDensity,
                longMethodDensity,
                deepNestingDensity,
                findingsDensity,
                cloneCoverage,
                averageFileLoc,
                averageFileSloc
        );
    }
}
