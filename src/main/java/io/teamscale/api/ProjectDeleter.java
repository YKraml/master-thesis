package io.teamscale.api;

import com.google.inject.Inject;
import io.teamscale.api.model.project.Project;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class ProjectDeleter {

    private final ProjectsFetcher projectsFetcher;
    private final HttpHelperTeamscale httpHelperTeamscale;

    @Inject
    public ProjectDeleter(ProjectsFetcher projectsFetcher, HttpHelperTeamscale httpHelperTeamscale) {
        this.projectsFetcher = projectsFetcher;
        this.httpHelperTeamscale = httpHelperTeamscale;
    }

    public void deleteAllProjects() {
        List<Project> existingProjects = projectsFetcher.getExistingProjects();
        for (Project existingProject : existingProjects) {
            String name = existingProject.getName();

            try {
                deleteProject(name);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void deleteProject(String projectName) throws IOException, InterruptedException {
        System.out.println("Deleting: " + projectName);

        String path = "/api/v2025.8/projects/"
                + projectName
                + "?force-delete=true&delete-all-assignments=true&delete-all-dashboards=true&delete-child-references=true";
        HttpResponse<String> response = httpHelperTeamscale.executeDeleteRequest(path);

        System.out.println("Status Code: " + response.statusCode());

    }

}
