package io.teamscale.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamscale.api.model.churn.FindingChurn;
import jakarta.inject.Inject;

import java.io.IOException;
import java.net.http.HttpResponse;

public class FindingChurnFetcher {

    private final HttpHelperTeamscale httpHelperTeamscale;

    @Inject
    public FindingChurnFetcher(HttpHelperTeamscale httpHelperTeamscale) {
        this.httpHelperTeamscale = httpHelperTeamscale;
    }

    public FindingChurn getFindingsChurn(String repoOwner, String repoName, long time) {
        try {
            return getFindingsChurnNotSafe(repoOwner, repoName, time);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private FindingChurn getFindingsChurnNotSafe(String repoOwner, String repoName, long time) throws IOException, InterruptedException {
        String path = "/api/v2025.8/projects/"
                + repoOwner + "." + repoName
                + "/finding-churn/list?&t=" + time + "&max=-1";

        HttpResponse<String> response = httpHelperTeamscale.executeGetRequest(path);

        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(response.body(), FindingChurn.class);
    }
}