
package teamscale.api.model.churn;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.processing.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "categoryInfos",
        "guidelineInfos"
})
@Generated("jsonschema2pojo")
public class FindingSummary {

    @JsonProperty("categoryInfos")
    private List<CategoryInfo> categoryInfos;
    @JsonProperty("guidelineInfos")
    private List<Object> guidelineInfos;

    @JsonProperty("categoryInfos")
    public List<CategoryInfo> getCategoryInfos() {
        return categoryInfos;
    }

    @JsonProperty("guidelineInfos")
    public List<Object> getGuidelineInfos() {
        return guidelineInfos;
    }

}
