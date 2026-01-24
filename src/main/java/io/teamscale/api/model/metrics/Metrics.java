package io.teamscale.api.model.metrics;

import java.util.List;

public record Metrics(long timestamp, List<Value> value) {

}
