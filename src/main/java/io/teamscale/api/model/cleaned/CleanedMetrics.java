package io.teamscale.api.model.cleaned;

public record CleanedMetrics(
        Integer timestamp,
        int files,
        int linesOfCode,
        int sourceLinesOfCode,
        double numberOfFindings,
        double cloneCoverage
) {
}