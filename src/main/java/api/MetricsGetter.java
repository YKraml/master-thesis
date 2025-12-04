package api;

import api.model.metrics.MetricValueDeserializer;
import api.model.metrics.Metrics;
import api.model.metrics.Value;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class MetricsGetter {

    @Inject
    public MetricsGetter() {
    }

    private static List<Metrics> getFindingsChurnThreadSafe(String repoOwner, String repoName, long analysisStartTime, long analysisEndTime) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(Constants.TEAMSCALE_URL + "/api/projects/" + repoOwner + "." + repoName + "/metrics/history?uniform-path=&start=" + analysisStartTime + "&end=" + analysisEndTime + "&all-partitions=true&max-milliseconds=-1");

            // Authentication
            ProjectCreator.addAuth(httpGet);

            // Headers
            httpGet.setHeader("accept", "application/json");

            // Execute the POST request
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                //int statusCode = response.getStatusLine().getStatusCode();
                //System.out.println("Response Code: " + statusCode);

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new IOException("Empty response entity");
                }

                String responseBody;
                try (InputStream contentStream = entity.getContent()) {
                    responseBody = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
                }

                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                SimpleModule module = new SimpleModule();
                module.addDeserializer(Value.class, new MetricValueDeserializer());
                mapper.registerModule(module);

                try {
                    return Arrays.stream(mapper.readValue(responseBody, Metrics[].class)).toList();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Metrics> getMetrics(String repoOwner, String repoName, long analysisStartTime, long analysisEndTime) {
        return getFindingsChurnThreadSafe(repoOwner, repoName, analysisStartTime, analysisEndTime);
    }
}
