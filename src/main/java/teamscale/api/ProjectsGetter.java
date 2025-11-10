package teamscale.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import teamscale.api.model.project.Project;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProjectsGetter {

    @Inject
    public ProjectsGetter() {
    }

    public List<Project> getExistingProjects() {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://thesis-yannick.teamscale.dev/api/v2025.8/projects?include-deleting=false&include-reanalyzing=false");

            // Authentication
            ProjectCreator.addAuth(httpGet);

            // Headers
            httpGet.setHeader("accept", "application/json");

            // Execute the POST request
            HttpEntity entity;
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Response Code: " + statusCode);
                entity = response.getEntity();

                if (entity == null) {
                    throw new IOException("Empty response entity");
                }

                String responseBody;
                try (InputStream contentStream = entity.getContent()) {
                    responseBody = new String(contentStream.readAllBytes(), StandardCharsets.UTF_8);
                }

                return List.of(new ObjectMapper().readValue(responseBody, Project[].class));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
