
package teamscale.api.model.churn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.processing.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "typeName",
        "groupName",
        "categoryName",
        "message",
        "location",
        "id",
        "birth",
        "codeScopeName",
        "assessment",
        "siblingLocations",
        "properties",
        "analysisTimestamp",
        "typeId",
        "tasksByStatus"
})
@Generated("jsonschema2pojo")
public class Finding {

    @JsonProperty("typeName")
    private String typeName;
    @JsonProperty("groupName")
    private String groupName;
    @JsonProperty("categoryName")
    private String categoryName;
    @JsonProperty("message")
    private String message;
    @JsonProperty("id")
    private String id;
    @JsonProperty("birth")
    private Birth birth;
    @JsonProperty("codeScopeName")
    private String codeScopeName;
    @JsonProperty("assessment")
    private String assessment;
    @JsonProperty("analysisTimestamp")
    private Integer analysisTimestamp;
    @JsonProperty("typeId")
    private String typeId;

    @JsonProperty("typeName")
    public String getTypeName() {
        return typeName;
    }

    @JsonProperty("groupName")
    public String getGroupName() {
        return groupName;
    }

    @JsonProperty("categoryName")
    public String getCategoryName() {
        return categoryName;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("birth")
    public Birth getBirth() {
        return birth;
    }

    @JsonProperty("codeScopeName")
    public String getCodeScopeName() {
        return codeScopeName;
    }

    @JsonProperty("assessment")
    public String getAssessment() {
        return assessment;
    }

    @JsonProperty("analysisTimestamp")
    public Integer getAnalysisTimestamp() {
        return analysisTimestamp;
    }

    @JsonProperty("typeId")
    public String getTypeId() {
        return typeId;
    }

}
