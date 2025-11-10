package teamscale.api;

import com.google.inject.Guice;
import com.google.inject.Inject;
import common.GuiceModule;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import teamscale.api.model.project.Project;

import java.util.List;

public class ProjectDeleter {

    private final ProjectsGetter projectsGetter;

    @Inject
    public ProjectDeleter(ProjectsGetter projectsGetter) {
        this.projectsGetter = projectsGetter;
    }

    public static void main(String[] args) {
        Guice.createInjector(new GuiceModule()).getInstance(ProjectDeleter.class).deleteAllProjects();
    }

    public void deleteAllProjects() {
        List<Project> existingProjects = projectsGetter.getExistingProjects();
        for (Project existingProject : existingProjects) {
            String name = existingProject.getName();
            deleteProject(name);
        }
    }

    public synchronized void deleteProject(String projectName) {
        System.out.println("Deleting: " + projectName);
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete("https://thesis-yannick.teamscale.dev/api/v2025.8/projects/" + projectName + "?force-delete=true&delete-all-assignments=true&delete-all-dashboards=true&delete-child-references=true");

            // Authentication
            ProjectCreator.addAuth(httpDelete);

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
        }
    }
}
