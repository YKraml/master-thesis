
package api.model.cleaned;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "timestamp",
        "totalNumberOfFiles",
        "totalNumberOfLinesOfCode",
        "totalNumberOfSourceLinesOfCode",
        "fileSizeAssessment",
        "methodLengthAssessment",
        "nestingDepthAssessment",
        "cloneCoverage"
})
@Generated("jsonschema2pojo")
public class CleanedMetrics {

    @JsonProperty("timestamp")
    private Integer timestamp;
    @JsonProperty("totalNumberOfFiles")
    private Integer totalNumberOfFiles;
    @JsonProperty("totalNumberOfLinesOfCode")
    private Integer totalNumberOfLinesOfCode;
    @JsonProperty("totalNumberOfSourceLinesOfCode")
    private Integer totalNumberOfSourceLinesOfCode;
    @JsonProperty("fileSizeAssessment")
    private Assessment fileSizeAssessment;
    @JsonProperty("methodLengthAssessment")
    private Assessment methodLengthAssessment;
    @JsonProperty("nestingDepthAssessment")
    private Assessment nestingDepthAssessment;
    @JsonProperty("cloneCoverage")
    private Double cloneCoverage;

    @JsonProperty("timestamp")
    public Integer getTimestamp() {
        return timestamp;
    }

    @JsonProperty("totalNumberOfFiles")
    public Integer getTotalNumberOfFiles() {
        return totalNumberOfFiles;
    }

    @JsonProperty("totalNumberOfLinesOfCode")
    public Integer getTotalNumberOfLinesOfCode() {
        return totalNumberOfLinesOfCode;
    }

    @JsonProperty("totalNumberOfSourceLinesOfCode")
    public Integer getTotalNumberOfSourceLinesOfCode() {
        return totalNumberOfSourceLinesOfCode;
    }

    @JsonProperty("fileSizeAssessment")
    public Assessment getFileSizeAssessment() {
        return fileSizeAssessment;
    }

    @JsonProperty("methodLengthAssessment")
    public Assessment getMethodLengthAssessment() {
        return methodLengthAssessment;
    }

    @JsonProperty("nestingDepthAssessment")
    public Assessment getNestingDepthAssessment() {
        return nestingDepthAssessment;
    }

    @JsonProperty("cloneCoverage")
    public Double getCloneCoverage() {
        return cloneCoverage;
    }

    public static CleanedMetrics of(Integer timestamp, Integer totalNumberOfFiles, Integer totalNumberOfLinesOfCode, Integer totalNumberOfSourceLinesOfCode, Assessment fileSizeAssessment, Assessment methodLengthAssessment, Assessment nestingDepthAssessment, Double cloneCoverage) {
        CleanedMetrics cleanedMetrics = new CleanedMetrics();
        cleanedMetrics.timestamp = timestamp;
        cleanedMetrics.totalNumberOfFiles = totalNumberOfFiles;
        cleanedMetrics.totalNumberOfLinesOfCode = totalNumberOfLinesOfCode;
        cleanedMetrics.totalNumberOfSourceLinesOfCode = totalNumberOfSourceLinesOfCode;
        cleanedMetrics.fileSizeAssessment = fileSizeAssessment;
        cleanedMetrics.methodLengthAssessment = methodLengthAssessment;
        cleanedMetrics.nestingDepthAssessment = nestingDepthAssessment;
        cleanedMetrics.cloneCoverage = cloneCoverage;
        return cleanedMetrics;
    }
}
