
package teamscale.api.model.metrics;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("uniformPath")
    private String uniformPath;
    @JsonProperty("metricValues")
    private List<MetricValue> metricValues;

    @JsonProperty("uniformPath")
    public String getUniformPath() {
        return uniformPath;
    }

    @JsonProperty("metricValues")
    public List<MetricValue> getMetricValues() {
        return metricValues;
    }
}
