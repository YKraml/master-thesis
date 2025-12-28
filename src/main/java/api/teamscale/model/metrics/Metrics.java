package api.teamscale.model.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "uniformPath",
        "metricValues",
})
@Generated("jsonschema2pojo")
public class Metrics {

    private long timestamp;
    private List<Value> value;

    public long getTimestamp() {
        return timestamp;
    }

    public List<Value> getValue() {
        return value;
    }
}
