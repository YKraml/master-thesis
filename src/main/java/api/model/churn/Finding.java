package api.model.churn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "groupName",
        "categoryName"
})
@Generated("jsonschema2pojo")
public class Finding {

    @JsonProperty("groupName")
    private String groupName;
    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("groupName")
    public String getGroupName() {
        return groupName;
    }

    @JsonProperty("categoryName")
    public String getCategoryName() {
        return categoryName;
    }

}
