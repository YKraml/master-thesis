package logic;

import api.model.churn.Finding;
import api.model.churn.FindingChurn;
import api.model.cleaned.Assessment;
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
        int totalNumberOfContributors = -1;
        int totalNumberOfFiles = cleanedMetrics.totalNumberOfFiles();
        int totalNumberOfLinesOfCode = cleanedMetrics.totalNumberOfLinesOfCode();
        int totalNumberOfSourceLinesOfCode = cleanedMetrics.totalNumberOfSourceLinesOfCode();
        int totalNumberOfCodeClones = beforeEntry.totalNumberOfCodeClones();
        int totalNumberOfFileSizeViolations = beforeEntry.totalNumberOfFileSizeViolations();
        int totalNumberOfMethodLengthViolations = beforeEntry.totalNumberOfMethodLengthViolations();
        int totalNumberOfNestingDepthViolations = beforeEntry.totalNumberOfNestingDepthViolations();

        for (Finding finding : findingChurn.getAddedFindings()) {
            String groupName = finding.getGroupName();
            switch (groupName) {
                case "Code Clones" -> totalNumberOfCodeClones++;
                case "File Size" -> totalNumberOfFileSizeViolations++;
                case "Method Length" -> totalNumberOfMethodLengthViolations++;
                case "Nesting Depth" -> totalNumberOfNestingDepthViolations++;
                default -> throw new IllegalStateException("Unexpected value: " + groupName);
            }
        }

        for (Finding finding : findingChurn.getRemovedFindings()) {
            String groupName = finding.getGroupName();
            switch (groupName) {
                case "Code Clones" -> totalNumberOfCodeClones--;
                case "File Size" -> totalNumberOfFileSizeViolations--;
                case "Method Length" -> totalNumberOfMethodLengthViolations--;
                case "Nesting Depth" -> totalNumberOfNestingDepthViolations--;
                default -> throw new IllegalStateException("Unexpected value: " + groupName);
            }
        }

        double relativeNumberOfCodeClones = (double) totalNumberOfCodeClones / totalNumberOfLinesOfCode * 1000;
        double relativeNumberOfFileSizeViolations = (double) totalNumberOfFileSizeViolations / totalNumberOfLinesOfCode * 1000;
        double relativeNumberOfMethodLengthViolations = (double) totalNumberOfMethodLengthViolations / totalNumberOfLinesOfCode * 1000;
        double relativeNumberOfNestingDepthViolations = (double) totalNumberOfNestingDepthViolations / totalNumberOfLinesOfCode * 1000;

        Assessment fileSizeAssessment = cleanedMetrics.fileSizeAssessment();
        int fileSizeAssessmentRed = fileSizeAssessment.getRed();
        int fileSizeAssessmentYellow = fileSizeAssessment.getYellow();
        int fileSizeAssessmentGreen = fileSizeAssessment.getGreen();

        Assessment methodLengthAssessment = cleanedMetrics.methodLengthAssessment();
        int methodLengthAssessmentRed = methodLengthAssessment.getRed();
        int methodLengthAssessmentYellow = methodLengthAssessment.getYellow();
        int methodLengthAssessmentGreen = methodLengthAssessment.getGreen();

        Assessment nestingDepthAssessment = cleanedMetrics.nestingDepthAssessment();
        int nestingDepthAssessmentRed = nestingDepthAssessment.getRed();
        int nestingDepthAssessmentYellow = nestingDepthAssessment.getYellow();
        int nestingDepthAssessmentGreen = nestingDepthAssessment.getGreen();

        double relativeFileSizeAssessmentRed = (double) fileSizeAssessmentRed / (fileSizeAssessmentRed + fileSizeAssessmentYellow + fileSizeAssessmentGreen);
        double relativeFileSizeAssessmentYellow = (double) fileSizeAssessmentYellow / (fileSizeAssessmentRed + fileSizeAssessmentYellow + fileSizeAssessmentGreen);
        double relativeFileSizeAssessmentGreen = (double) fileSizeAssessmentGreen / (fileSizeAssessmentRed + fileSizeAssessmentYellow + fileSizeAssessmentGreen);
        double relativeMethodLengthAssessmentRed = (double) methodLengthAssessmentRed / (methodLengthAssessmentRed + methodLengthAssessmentYellow + methodLengthAssessmentGreen);
        double relativeMethodLengthAssessmentYellow = (double) methodLengthAssessmentYellow / (methodLengthAssessmentRed + methodLengthAssessmentYellow + methodLengthAssessmentGreen);
        double relativeMethodLengthAssessmentGreen = (double) methodLengthAssessmentGreen / (methodLengthAssessmentRed + methodLengthAssessmentYellow + methodLengthAssessmentGreen);
        double relativeNestingDepthAssessmentRed = (double) nestingDepthAssessmentRed / (nestingDepthAssessmentRed + nestingDepthAssessmentYellow + nestingDepthAssessmentGreen);
        double relativeNestingDepthAssessmentYellow = (double) nestingDepthAssessmentYellow / (nestingDepthAssessmentRed + nestingDepthAssessmentYellow + nestingDepthAssessmentGreen);
        double relativeNestingDepthAssessmentGreen = (double) nestingDepthAssessmentGreen / (nestingDepthAssessmentRed + nestingDepthAssessmentYellow + nestingDepthAssessmentGreen);

        double cloneCoverage = cleanedMetrics.cloneCoverage();
        double averageFileLoc = (double) totalNumberOfSourceLinesOfCode / totalNumberOfFiles;
        double averageFileSloc = (double) totalNumberOfLinesOfCode / totalNumberOfFiles;

        return new Result.Entry(
                time,
                totalNumberOfContributors,
                totalNumberOfFiles,
                totalNumberOfLinesOfCode,
                totalNumberOfSourceLinesOfCode,
                totalNumberOfCodeClones,
                totalNumberOfFileSizeViolations,
                totalNumberOfMethodLengthViolations,
                totalNumberOfNestingDepthViolations,
                relativeNumberOfCodeClones,
                relativeNumberOfFileSizeViolations,
                relativeNumberOfMethodLengthViolations,
                relativeNumberOfNestingDepthViolations,
                fileSizeAssessmentRed,
                fileSizeAssessmentYellow,
                fileSizeAssessmentGreen,
                methodLengthAssessmentRed,
                methodLengthAssessmentYellow,
                methodLengthAssessmentGreen,
                nestingDepthAssessmentRed,
                nestingDepthAssessmentYellow,
                nestingDepthAssessmentGreen,
                relativeFileSizeAssessmentRed,
                relativeFileSizeAssessmentYellow,
                relativeFileSizeAssessmentGreen,
                relativeMethodLengthAssessmentRed,
                relativeMethodLengthAssessmentYellow,
                relativeMethodLengthAssessmentGreen,
                relativeNestingDepthAssessmentRed,
                relativeNestingDepthAssessmentYellow,
                relativeNestingDepthAssessmentGreen,
                cloneCoverage,
                averageFileLoc,
                averageFileSloc
        );
    }
}
