package api;

import api.model.churn.FindingChurn;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class FindingChurnGetter {

    @Inject
    public FindingChurnGetter() {
    }

    public FindingChurn getFindingsChurn(String repoOwner, String repoName, long time) {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(Constants.TEAMSCALE_URL + "/api/v2025.8/projects/" + repoOwner + "." + repoName + "/finding-churn/list?&t=" + time + "&max=-1");

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
                return mapper.readValue(responseBody, FindingChurn.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}