package api;

import api.model.project.Project;
import com.google.inject.Inject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import util.Constants;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class ProjectDeleter {

    private final ProjectsFetcher projectsFetcher;

    @Inject
    public ProjectDeleter(ProjectsFetcher projectsFetcher) {
        this.projectsFetcher = projectsFetcher;
    }

    public void deleteAllProjects() {
        List<Project> existingProjects = projectsFetcher.getExistingProjects();
        for (Project existingProject : existingProjects) {
            String name = existingProject.getName();
            deleteProject(name);
        }
    }

    public synchronized void deleteProject(String projectName) {
        System.out.println("Deleting: " + projectName);
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete(Constants.TEAMSCALE_URL + "/api/v2025.8/projects/" + projectName + "?force-delete=true&delete-all-assignments=true&delete-all-dashboards=true&delete-child-references=true");

            // Authentication
            String auth = Constants.TEAMSCALE_USER + ":" + Constants.TEAMSCALE_ACCESS_KEY;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.ISO_8859_1));
            String authHeader = "Basic " + new String(encodedAuth);
            httpDelete.setHeader("Authorization", authHeader);

            // Headers
            httpDelete.setHeader("accept", "*/*");

            // Execute the POST request
            int statusCode;
            try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
                statusCode = response.getStatusLine().getStatusCode();
            }
            System.out.println("Response Code: " + statusCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
