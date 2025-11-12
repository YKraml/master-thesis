
package teamscale.api.model.churn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.processing.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "typeIdName",
        "count",
        "countRed"
})
@Generated("jsonschema2pojo")
public class TypeIdInfo {

    @JsonProperty("name")
    private String name;
    @JsonProperty("typeIdName")
    private String typeIdName;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("countRed")
    private Integer countRed;

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("typeIdName")
    public String getTypeIdName() {
        return typeIdName;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    @JsonProperty("countRed")
    public Integer getCountRed() {
        return countRed;
    }

}
