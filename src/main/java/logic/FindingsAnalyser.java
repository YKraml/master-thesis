package logic;

import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import api.FindingChurnGetter;
import api.MetricsGetter;
import api.model.churn.Finding;
import api.model.churn.FindingChurn;
import api.model.metrics.Metrics;
import api.model.metrics.ValueDouble;
import model.Result;

import java.util.ArrayList;
import java.util.List;

public class FindingsAnalyser {

    private final State state;
    private final FindingChurnGetter findingChurnGetter;
    private final MetricsGetter metricsGetter;

    @Inject
    public FindingsAnalyser(State state, FindingChurnGetter findingChurnGetter, MetricsGetter metricsGetter) {
        this.state = state;
        this.findingChurnGetter = findingChurnGetter;
        this.metricsGetter = metricsGetter;
    }

    public Result analyseFindings(String repoOwner, String repoName) {
        Pair<String, String> stateKey = Pair.of(repoOwner, repoName);

        long start = 1;
        long end = Long.MAX_VALUE;

        state.addToState(stateKey, "Getting metrics list");
        List<Metrics> metricsList = metricsGetter.getMetrics(repoOwner, repoName, start, end);

        List<Pair<FindingChurn, Metrics>> data = new ArrayList<>();
        for (int i = 0; i < metricsList.size(); i++) {
            state.addToState(stateKey, "Analysing finding from commit " + i + "/" + metricsList.size());
            Metrics metrics = metricsList.get(i);
            FindingChurn findingChurn = findingChurnGetter.getFindingsChurn(repoOwner, repoName, metrics.getTimestamp());

            if (metrics.getTimestamp() != findingChurn.getCommit().getTimestamp()) {
                throw new RuntimeException("Timestamps not equal!");
            }

            data.add(Pair.of(findingChurn, metrics));
        }

        List<Result.Entry> entries = createEntriesFromData(stateKey, data);
        return new Result(repoOwner, repoName, entries);
    }

    private List<Result.Entry> createEntriesFromData(Pair<String, String> key, List<Pair<FindingChurn, Metrics>> data) {
        List<Result.Entry> entries = new ArrayList<>();
        Result.Entry beforeEntry = Result.Entry.getDefault();

        for (int i = 0; i < data.size(); i++) {
            Pair<FindingChurn, Metrics> datum = data.get(i);
            state.addToState(key, "Converting data from commit " + i + "/" + data.size());
            Result.Entry entryFromData = createEntryFromData(datum.getLeft(), datum.getRight(), beforeEntry);
            beforeEntry = entryFromData;
            entries.add(entryFromData);
        }

        return entries;
    }

    private Result.Entry createEntryFromData(FindingChurn findingChurn, Metrics metrics, Result.Entry beforeEntry) {
        int time = Math.toIntExact(metrics.getTimestamp() / 1000);
        int totalNumberOfContributors = -1;
        int totalNumberOfFiles = (int) ((ValueDouble) metrics.getValue().get(0)).getValue();
        int totalNumberOfLinesOfCode = (int) ((ValueDouble) metrics.getValue().get(1)).getValue();
        int totalNumberOfSourceLinesOfCode = (int) ((ValueDouble) metrics.getValue().get(2)).getValue();
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
                relativeNumberOfNestingDepthViolations);
    }
}
