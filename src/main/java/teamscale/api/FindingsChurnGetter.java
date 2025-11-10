package teamscale.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import teamscale.api.model.churn.FindingsChurn;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

public class FindingsChurnGetter {

    private final Semaphore semaphore = new Semaphore(4);

    @Inject
    public FindingsChurnGetter() {
    }

    public FindingsChurn getFindingsChurn(String repoOwner, String repoName, long time) {

        try {
            semaphore.acquire();
            return getFindingsChurnThreadSafe(repoOwner, repoName, time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }

    }

    private static FindingsChurn getFindingsChurnThreadSafe(String repoOwner, String repoName, long time) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://thesis-yannick.teamscale.dev/api/v2025.8/projects/" + repoOwner + "." + repoName + "/finding-churn/list?&t=" + time + "&max=-1");

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
                return mapper.readValue(responseBody, FindingsChurn.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
