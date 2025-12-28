package api.teamscale.model.cleaned;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "red",
        "yellow",
        "green"
})
@Generated("jsonschema2pojo")
public class Assessment {

    @JsonProperty("red")
    private Integer red;
    @JsonProperty("yellow")
    private Integer yellow;
    @JsonProperty("green")
    private Integer green;

    public static Assessment of(Integer red, Integer yellow, Integer green) {
        Assessment assessment = new Assessment();
        assessment.red = red;
        assessment.yellow = yellow;
        assessment.green = green;
        return assessment;
    }

    @JsonProperty("red")
    public Integer getRed() {
        return red;
    }

    @JsonProperty("yellow")
    public Integer getYellow() {
        return yellow;
    }

    @JsonProperty("green")
    public Integer getGreen() {
        return green;
    }
}
