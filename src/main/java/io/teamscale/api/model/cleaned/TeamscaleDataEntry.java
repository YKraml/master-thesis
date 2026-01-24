package io.teamscale.api.model.cleaned;

import io.teamscale.api.model.churn.FindingChurn;

public record TeamscaleDataEntry(FindingChurn findingChurn, CleanedMetrics metrics) {

}