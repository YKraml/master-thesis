package api.model.cleaned;

public record CleanedMetrics(
        Integer timestamp,
        int files,
        int linesOfCode,
        int sourceLinesOfCode,
        Assessment fileSizeAssessment,
        int longestMethodLength,
        Assessment methodLengthAssessment,
        int maximumNestingDepth,
        Assessment nestingDepthAssessment,
        double changeCount,
        double numberOfFindings,
        double numberOfFindingsRed,
        double numberOfFindingsYellow,
        double findingsDensity,
        double findingsDensityRed,
        double findingsDensityYellow,
        double cloneCoverage,
        Assessment commentCompletenessAssessment,
        double lineCoverage,
        double findingsDensityInArchitectureRed,
        double findingsDensityInArchitectureYellow,
        double numberOfFindingsInComprehensibilityRed,
        double findingsDensityInComprehensibility,
        double numberOfFindingsInCorrectnessRed,
        double findingsDensityInCorrectness,
        double numberOfFindingsInDocumentationRed,
        double findingsDensityInDocumentation,
        double numberOfFindingsInErrorHandlingRed,
        double findingsDensityInErrorHandling
) {
}