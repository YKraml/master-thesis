package io.teamscale.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamscale.api.model.commit.chart.CommitChart;
import jakarta.inject.Inject;
import util.Constants;

import java.io.IOException;
import java.net.http.HttpResponse;

public class CommitChartFetcher {

    private final HttpHelperTeamscale httpHelperTeamscale;

    @Inject
    public CommitChartFetcher(HttpHelperTeamscale httpHelperTeamscale) {
        this.httpHelperTeamscale = httpHelperTeamscale;
    }

    public CommitChart getCommitChart(String repoOwner, String repoName) {
        try {
            return getCommitChartNotSafe(repoOwner, repoName);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CommitChart getCommitChartNotSafe(String repoOwner, String repoName) throws IOException, InterruptedException {
        String url = Constants.TEAMSCALE_URL
                + "/api/projects/"
                + repoOwner + "." + repoName
                + "/commit-chart?t1=1&max-milliseconds=-1";

        HttpResponse<String> response = httpHelperTeamscale.getResponse(url);
        return new ObjectMapper().readValue(response.body(), CommitChart.class);
    }
}
