package teamscale.model.churn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Result {

    private final String repoOwner;
    private final String repoName;
    private final List<Entry> entries;

    //Churns are sorted by time
    public Result(String repoOwner, String repoName, List<FindingsChurn> churns) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        entries = new ArrayList<>();

        for (int i = 0; i < churns.size(); i++) {
            FindingsChurn churn = churns.get(i);

            int numberOfCodeClones = 0;
            int numberOfFileSizeViolations = 0;
            int numberOfMethodLengthViolations = 0;
            int numberOfNestingDepthViolations = 0;
            for (Finding addedFinding : churn.getAddedFindings()) {
                String groupName = addedFinding.getGroupName();
                switch (groupName) {
                    case "Code Clones" -> numberOfCodeClones++;
                    case "File Size" -> numberOfFileSizeViolations++;
                    case "Method Length" -> numberOfMethodLengthViolations++;
                    case "Nesting Depth" -> numberOfNestingDepthViolations++;
                    default -> throw new IllegalStateException("Unexpected value: " + groupName);
                }
            }

            for (Finding addedFinding : churn.getFindingsAddedInBranch()) {
                String groupName = addedFinding.getGroupName();
                switch (groupName) {
                    case "Code Clones" -> numberOfCodeClones++;
                    case "File Size" -> numberOfFileSizeViolations++;
                    case "Method Length" -> numberOfMethodLengthViolations++;
                    case "Nesting Depth" -> numberOfNestingDepthViolations++;
                    default -> throw new IllegalStateException("Unexpected value: " + groupName);
                }
            }

            for (Finding removedFinding : churn.getRemovedFindings()) {
                String groupName = removedFinding.getGroupName();
                switch (groupName) {
                    case "Code Clones" -> numberOfCodeClones--;
                    case "File Size" -> numberOfFileSizeViolations--;
                    case "Method Length" -> numberOfMethodLengthViolations--;
                    case "Nesting Depth" -> numberOfNestingDepthViolations--;
                    default -> throw new IllegalStateException("Unexpected value: " + groupName);
                }
            }

            for (Finding removedFinding : churn.getFindingsRemovedInBranch()) {
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

    public String getRepoName() {
        return repoName;
    }

    public String getRepoOwner() {
        return repoOwner;
    }

    public record Entry(int time, int numberOfCodeClones, int numberOfFileSizeViolations,
                        int numberOfMethodLengthViolations, int numberOfNestingDepthViolations) {
    }
}
