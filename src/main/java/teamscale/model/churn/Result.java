package teamscale.model.churn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Result {

    private final List<Entry> entries;

    //Churns are sorted by time
    public Result(List<FindingsChurn> churns) {
        entries = new ArrayList<>();

        for (int i = 0; i < churns.size(); i++) {
            FindingsChurn churn = churns.get(i);
            List<Finding> addedFindings = churn.getAddedFindings();
            List<Finding> removedFindings = churn.getRemovedFindings();

            int numberOfCodeClones = 0;
            int numberOfFileSizeViolations = 0;
            int numberOfMethodLengthViolations = 0;
            int numberOfNestingDepthViolations = 0;
            for (Finding addedFinding : addedFindings) {
                String groupName = addedFinding.getGroupName();
                switch (groupName) {
                    case "Code Clones" -> numberOfCodeClones++;
                    case "File Size" -> numberOfFileSizeViolations++;
                    case "Method Length" -> numberOfMethodLengthViolations++;
                    case "Nesting Depth" -> numberOfNestingDepthViolations++;
                    default -> throw new IllegalStateException("Unexpected value: " + groupName);
                }
            }

            for (Finding removedFinding : removedFindings) {
                String groupName = removedFinding.getGroupName();
                switch (groupName) {
                    case "Code Clones" -> numberOfCodeClones--;
                    case "File Size" -> numberOfFileSizeViolations--;
                    case "Method Length" -> numberOfMethodLengthViolations--;
                    case "Nesting Depth" -> numberOfNestingDepthViolations--;
                    default -> throw new IllegalStateException("Unexpected value: " + groupName);
                }
            }

            if (entries.isEmpty()) {
                entries.add(new Entry(Math.toIntExact(churn.getCommit().getTimestamp() / 1000), numberOfCodeClones, numberOfFileSizeViolations, numberOfMethodLengthViolations, numberOfNestingDepthViolations));
            } else {
                Entry beforeEntry = entries.get(i - 1);
                entries.add(new Entry(Math.toIntExact(churn.getCommit().getTimestamp() / 1000),
                        beforeEntry.numberOfCodeClones + numberOfCodeClones,
                        beforeEntry.numberOfFileSizeViolations + numberOfFileSizeViolations,
                        beforeEntry.numberOfMethodLengthViolations + numberOfMethodLengthViolations,
                        beforeEntry.numberOfNestingDepthViolations + numberOfNestingDepthViolations));
            }

        }
    }

    public void addEntry(int time, int numberOfCodeClones, int numberOfFileSizeViolations, int numberOfMethodLengthViolations, int numberOfNestingDepthViolations) {
        entries.add(new Entry(time, numberOfCodeClones, numberOfFileSizeViolations, numberOfMethodLengthViolations, numberOfNestingDepthViolations));
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public record Entry(int time, int numberOfCodeClones, int numberOfFileSizeViolations,
                        int numberOfMethodLengthViolations, int numberOfNestingDepthViolations) {
    }
}
