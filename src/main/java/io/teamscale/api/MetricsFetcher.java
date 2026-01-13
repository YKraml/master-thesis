package io.teamscale.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.teamscale.api.model.metrics.MetricValueDeserializer;
import io.teamscale.api.model.metrics.Metrics;
import io.teamscale.api.model.metrics.Value;
import jakarta.inject.Inject;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class MetricsFetcher {

    private final HttpHelperTeamscale httpHelperTeamscale;

    @Inject
    public MetricsFetcher(HttpHelperTeamscale httpHelperTeamscale) {
        this.httpHelperTeamscale = httpHelperTeamscale;
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Value.class, new MetricValueDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    public List<Metrics> getMetrics(String repoOwner, String repoName, long analysisStartTime, long analysisEndTime) {
        try {
            return getMetricsNotSafe(repoOwner, repoName, analysisStartTime, analysisEndTime);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Metrics> getMetricsNotSafe(String repoOwner, String repoName, long analysisStartTime, long analysisEndTime)
            throws IOException, InterruptedException {
        String path = "/api/projects/" + repoOwner + "." + repoName
                + "/metrics/history?uniform-path=&start=" + analysisStartTime
                + "&end=" + analysisEndTime
                + "&all-partitions=true&max-milliseconds=-1";

        HttpResponse<String> response = httpHelperTeamscale.executeGetRequest(path);

        return Arrays.asList(createMapper().readValue(response.body(), Metrics[].class));
    }
}
