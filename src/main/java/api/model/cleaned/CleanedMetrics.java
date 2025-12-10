package api.model.cleaned;

public record CleanedMetrics(Integer timestamp, Integer totalNumberOfFiles, Integer totalNumberOfLinesOfCode,
                             Integer totalNumberOfSourceLinesOfCode, Assessment fileSizeAssessment,
                             Assessment methodLengthAssessment, Assessment nestingDepthAssessment,
                             Double cloneCoverage) {

}
