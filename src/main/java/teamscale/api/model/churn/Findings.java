
package teamscale.api.model.churn;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.processing.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "findings",
        "summary"
})
@Generated("jsonschema2pojo")
public class Findings {

    @JsonProperty("findings")
    private List<Finding> findings;

    @JsonProperty("findings")
    public List<Finding> getFindings() {
        return findings;
    }

}
