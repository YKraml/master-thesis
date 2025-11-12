
package teamscale.api.model.churn;

import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.processing.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "groupName",
        "count",
        "countRed",
        "typeIdInfos"
})
@Generated("jsonschema2pojo")
public class GroupInfo {

    @JsonProperty("groupName")
    private String groupName;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("countRed")
    private Integer countRed;
    @JsonProperty("typeIdInfos")
    private List<TypeIdInfo> typeIdInfos;

    @JsonProperty("groupName")
    public String getGroupName() {
        return groupName;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    @JsonProperty("countRed")
    public Integer getCountRed() {
        return countRed;
    }

    @JsonProperty("typeIdInfos")
    public List<TypeIdInfo> getTypeIdInfos() {
        return typeIdInfos;
    }

}
