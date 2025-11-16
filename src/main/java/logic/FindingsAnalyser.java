package logic;

import api.FindingChurnGetter;
import api.MetricsGetter;
import api.model.churn.Finding;
import api.model.churn.FindingChurn;
import api.model.metrics.Metrics;
import api.model.metrics.ValueDouble;
import api.model.metrics.ValueMapping;
import jakarta.inject.Inject;
import model.Result;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class FindingsAnalyser {

    private final static Semaphore SEMAPHORE = new Semaphore(4);

    private final ExecutorService executorService;
    private final State state;
    private final FindingChurnGetter findingChurnGetter;
    private final MetricsGetter metricsGetter;

    @Inject
    public FindingsAnalyser(ExecutorService executorService, State state, FindingChurnGetter findingChurnGetter, MetricsGetter metricsGetter) {
        this.executorService = executorService;
        this.state = state;
        this.findingChurnGetter = findingChurnGetter;
        this.metricsGetter = metricsGetter;
    }

    public Result analyseFindings(String repoOwner, String repoName) {
        Pair<String, String> stateKey = Pair.of(repoOwner, repoName);
        state.addToState(stateKey, "Getting metrics list");

        List<Metrics> metricsList = metricsGetter.getMetrics(repoOwner, repoName, 1, Long.MAX_VALUE);

        List<CompletableFuture<?>> futures = Collections.synchronizedList(new ArrayList<>());
        List<Pair<FindingChurn, Metrics>> data = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (Metrics metrics : metricsList) {
            futures.add(CompletableFuture.runAsync(() -> {

                try {
                    SEMAPHORE.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                state.addToState(stateKey, "Analysing finding from commit " + atomicInteger.get() + "/" + metricsList.size());
                FindingChurn findingChurn = findingChurnGetter.getFindingsChurn(repoOwner, repoName, metrics.getTimestamp());
                data.add(Pair.of(findingChurn, metrics));
                atomicInteger.incrementAndGet();

                SEMAPHORE.release();
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
        data.sort((o1, o2) -> Math.toIntExact(o1.getRight().getTimestamp() / 1000 - o2.getRight().getTimestamp() / 1000));

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

        ValueMapping fileSizeAssessment = (ValueMapping) metrics.getValue().get(3);
        int fileSizeAssessmentRed = (int) fileSizeAssessment.getA();
        int fileSizeAssessmentYellow = (int) fileSizeAssessment.getYellow();
        int fileSizeAssessmentGreen = (int) fileSizeAssessment.getGreen();

        ValueMapping methodLengthAssessment = (ValueMapping) metrics.getValue().get(5);
        int methodLengthAssessmentRed = (int) methodLengthAssessment.getA();
        int methodLengthAssessmentYellow = (int) methodLengthAssessment.getYellow();
        int methodLengthAssessmentGreen = (int) methodLengthAssessment.getGreen();

        ValueMapping nestingDepthAssessment = (ValueMapping) metrics.getValue().get(7);
        int nestingDepthAssessmentRed = (int) nestingDepthAssessment.getA();
        int nestingDepthAssessmentYellow = (int) nestingDepthAssessment.getYellow();
        int nestingDepthAssessmentGreen = (int) nestingDepthAssessment.getGreen();

        double relativeFileSizeAssessmentRed = (double) fileSizeAssessmentRed / (fileSizeAssessmentRed + fileSizeAssessmentYellow + fileSizeAssessmentGreen);
        double relativeFileSizeAssessmentYellow = (double) fileSizeAssessmentYellow / (fileSizeAssessmentRed + fileSizeAssessmentYellow + fileSizeAssessmentGreen);
        double relativeFileSizeAssessmentGreen = (double) fileSizeAssessmentGreen / (fileSizeAssessmentRed + fileSizeAssessmentYellow + fileSizeAssessmentGreen);
        double relativeMethodLengthAssessmentRed = (double) methodLengthAssessmentRed / (methodLengthAssessmentRed + methodLengthAssessmentYellow + methodLengthAssessmentGreen);
        double relativeMethodLengthAssessmentYellow = (double) methodLengthAssessmentYellow / (methodLengthAssessmentRed + methodLengthAssessmentYellow + methodLengthAssessmentGreen);
        double relativeMethodLengthAssessmentGreen = (double) methodLengthAssessmentGreen / (methodLengthAssessmentRed + methodLengthAssessmentYellow + methodLengthAssessmentGreen);
        double relativeNestingDepthAssessmentRed = (double) nestingDepthAssessmentRed / (nestingDepthAssessmentRed + nestingDepthAssessmentYellow + nestingDepthAssessmentGreen);
        double relativeNestingDepthAssessmentYellow = (double) nestingDepthAssessmentYellow / (nestingDepthAssessmentRed + nestingDepthAssessmentYellow + nestingDepthAssessmentGreen);
        double relativeNestingDepthAssessmentGreen = (double) nestingDepthAssessmentGreen / (nestingDepthAssessmentRed + nestingDepthAssessmentYellow + nestingDepthAssessmentGreen);

        double cloneCoverage = ((ValueDouble) metrics.getValue().get(17)).getValue();

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
                cloneCoverage
        );
    }
}
