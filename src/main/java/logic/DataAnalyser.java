package logic;

import api.model.churn.Finding;
import api.model.churn.FindingChurn;
import api.model.cleaned.CleanedMetrics;
import api.model.cleaned.TeamscaleData;
import api.model.cleaned.TeamscaleDataEntry;
import com.google.inject.Inject;
import model.Result;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class DataAnalyser {

    private final StateHandler stateHandler;

    @Inject
    public DataAnalyser(StateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    public Result analyseTeamscaleData(TeamscaleData data) {
        String repoOwner = data.getRepoOwner();
        String repoName = data.getRepoName();

        return new Result(repoOwner, repoName, createEntriesFromData(repoOwner, repoName, data));
    }

    private List<Result.Entry> createEntriesFromData(String repoOwner, String repoName, TeamscaleData teamscaleData) {
        List<Result.Entry> resultEntries = new ArrayList<>();
        List<TeamscaleDataEntry> dataEntries = teamscaleData.getEntries();

        Result.Entry beforeEntry = Result.Entry.getDefault();
        for (int i = 0; i < dataEntries.size(); i++) {
            TeamscaleDataEntry datum = dataEntries.get(i);
            stateHandler.addToState(Pair.of(repoOwner, repoName), "Converting entry from commit " + i + "/" + dataEntries.size());
            Result.Entry entryFromData = createEntryFromData(datum.getFindingChurn(), datum.getMetrics(), beforeEntry);
            beforeEntry = entryFromData;
            resultEntries.add(entryFromData);
        }

        return resultEntries;
    }

    private Result.Entry createEntryFromData(FindingChurn findingChurn, CleanedMetrics cleanedMetrics, Result.Entry beforeEntry) {
        int time = Math.toIntExact(cleanedMetrics.timestamp());
        int files = cleanedMetrics.files();
        int linesOfCode = cleanedMetrics.linesOfCode();
        int sourceLinesOfCode = cleanedMetrics.sourceLinesOfCode();
        int findings = (int) cleanedMetrics.numberOfFindings();

        // Finding categories
        int comprehensibility = 0;
        int correctness = 0;
        int documentation = 0;
        int efficiency = 0;
        int errorHandling = 0;
        int redundancy = 0;
        int structure = 0;
        for (Finding finding : findingChurn.removedFindings()) {
            switch (finding.categoryName()) {
                case "Comprehensibility" -> comprehensibility++;
                case "Correctness" -> correctness++;
                case "Documentation" -> documentation++;
                case "Efficiency" -> efficiency++;
                case "Error Handling" -> errorHandling++;
                case "Redundancy" -> redundancy++;
                case "Structure" -> structure++;
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
                default -> throw new IllegalStateException("Unexpected value: " + finding.categoryName());
            }
        }
        int comprehensibilityDensity = comprehensibility / sourceLinesOfCode * 1000;
        int correctnessDensity = correctness / sourceLinesOfCode * 1000;
        int documentationDensity = documentation / sourceLinesOfCode * 1000;
        int efficiencyDensity = efficiency / sourceLinesOfCode * 1000;
        int errorHandlingDensity = errorHandling / sourceLinesOfCode * 1000;
        int redundancyDensity = redundancy / sourceLinesOfCode * 1000;
        int structureDensity = structure / sourceLinesOfCode * 1000;

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
        double cloneCoverage = cleanedMetrics.cloneCoverage();
        double averageFileLoc = (double) sourceLinesOfCode / files;
        double averageFileSloc = (double) linesOfCode / files;

        return new Result.Entry(
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
                comprehensibilityDensity,
                correctnessDensity,
                documentationDensity,
                efficiencyDensity,
                errorHandlingDensity,
                redundancyDensity,
                structureDensity,
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
