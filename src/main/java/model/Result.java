package model;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record Result(String repoOwner, String repoName, List<Entry> entries) {

    public Result(String repoOwner, String repoName, List<Entry> entries) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.entries = new ArrayList<>(entries);
    }

    @Override
    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public record Entry(
            int time,
            int totalNumberOfContributors,
            int totalNumberOfFiles,
            int totalNumberOfLinesOfCode,
            int totalNumberOfSourceLinesOfCode,
            int totalNumberOfCodeClones,
            int totalNumberOfFileSizeViolations,
            int totalNumberOfMethodLengthViolations,
            int totalNumberOfNestingDepthViolations,
            double relativeNumberOfCodeClones,
            double relativeNumberOfFileSizeViolations,
            double relativeNumberOfMethodLengthViolations,
            double relativeNumberOfNestingDepthViolations,
            int fileSizeAssessmentRed,
            int fileSizeAssessmentYellow,
            int fileSizeAssessmentGreen,
            int methodLengthAssessmentRed,
            int methodLengthAssessmentYellow,
            int methodLengthAssessmentGreen,
            int nestingDepthAssessmentRed,
            int nestingDepthAssessmentYellow,
            int nestingDepthAssessmentGreen,
            double relativeFileSizeAssessmentRed,
            double relativeFileSizeAssessmentYellow,
            double relativeFileSizeAssessmentGreen,
            double relativeMethodLengthAssessmentRed,
            double relativeMethodLengthAssessmentYellow,
            double relativeMethodLengthAssessmentGreen,
            double relativeNestingDepthAssessmentRed,
            double relativeNestingDepthAssessmentYellow,
            double relativeNestingDepthAssessmentGreen,
            double cloneCoverage,
            double averageFileLoc,
            double averageFileSloc
    ) {

        public static String csvHeader() {
            return Arrays.stream(Entry.class.getRecordComponents())
                    .map(RecordComponent::getName)
                    .collect(Collectors.joining(","));
        }

        public static Entry getDefault() {
            return new Result.Entry(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public String csvLine() {
            try {
                return Arrays.stream(this.getClass().getRecordComponents())
                        .map(recordComponent -> {
                            try {
                                return String.valueOf(recordComponent.getAccessor().invoke(this));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.joining(","));
            } catch (Exception e) {
                throw new RuntimeException("Error creating CSV string", e);
            }
        }
    }
}
