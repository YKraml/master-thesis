
package teamscale.api.model.churn;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.processing.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "categoryName",
        "count",
        "countRed",
        "groupInfos"
})
@Generated("jsonschema2pojo")
public class CategoryInfo {

    @JsonProperty("categoryName")
    private String categoryName;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("countRed")
    private Integer countRed;
    @JsonProperty("groupInfos")
    private List<GroupInfo> groupInfos;

    @JsonProperty("categoryName")
    public String getCategoryName() {
        return categoryName;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    @JsonProperty("countRed")
    public Integer getCountRed() {
        return countRed;
    }

    @JsonProperty("groupInfos")
    public List<GroupInfo> getGroupInfos() {
        return groupInfos;
    }

}
