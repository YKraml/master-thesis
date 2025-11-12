package teamscale.model;

import org.apache.commons.lang3.tuple.Pair;
import teamscale.api.model.churn.Churn;
import teamscale.api.model.metrics.ValueDouble;
import teamscale.api.model.metrics.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Result {

    private final String repoOwner;
    private final String repoName;
    private final List<Entry> entries;

    //Churns are sorted by time
    public Result(String repoOwner, String repoName, List<Pair<Churn, Metrics>> data) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        entries = new ArrayList<>();



    }

    public void addEntry(int time, int sourceLinesOfCode, int numberOfCodeClones, int numberOfFileSizeViolations, int numberOfMethodLengthViolations, int numberOfNestingDepthViolations) {
        entries.add(new Entry(time, sourceLinesOfCode, numberOfCodeClones, numberOfFileSizeViolations, numberOfMethodLengthViolations, numberOfNestingDepthViolations));
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

    public record Entry(int time, int sourceLinesOfCode, int numberOfCodeClones, int numberOfFileSizeViolations,
                        int numberOfMethodLengthViolations, int numberOfNestingDepthViolations) {
    }
}
