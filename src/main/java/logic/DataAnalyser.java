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

    private final State state;

    @Inject
    public DataAnalyser(State state) {
        this.state = state;
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
            state.addToState(Pair.of(repoOwner, repoName), "Converting entry from commit " + i + "/" + dataEntries.size());
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

        int clones = beforeEntry.clones();
        int longFiles = beforeEntry.longFiles();
        int longMethods = beforeEntry.longMethods();
        int deepNesting = beforeEntry.deepNesting();
        for (Finding finding : findingChurn.addedFindings()) {
            switch (finding.groupName()) {
                case "Code Clones" -> clones++;
                case "File Size" -> longFiles++;
                case "Method Length" -> longMethods++;
                case "Nesting Depth" -> deepNesting++;
                default -> throw new IllegalStateException("Unexpected value: " + finding.groupName());
            }
        }
        for (Finding finding : findingChurn.removedFindings()) {
            switch (finding.groupName()) {
                case "Code Clones" -> clones--;
                case "File Size" -> longFiles--;
                case "Method Length" -> longMethods--;
                case "Nesting Depth" -> deepNesting--;
                default -> throw new IllegalStateException("Unexpected value: " + finding.groupName());
            }
        }

        int findings = (int) cleanedMetrics.numberOfFindings();

        double clonesDensity = (double) clones / sourceLinesOfCode * 1000;
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
                clones,
                longFiles,
                longMethods,
                deepNesting,
                findings,
                clonesDensity,
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
