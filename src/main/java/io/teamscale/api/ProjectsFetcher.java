package io.teamscale.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamscale.api.model.project.Project;
import jakarta.inject.Inject;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;

public class ProjectsFetcher {

    private final HttpHelperTeamscale httpHelperTeamscale;

    @Inject
    public ProjectsFetcher(HttpHelperTeamscale httpHelperTeamscale) {
        this.httpHelperTeamscale = httpHelperTeamscale;
    }

    public List<Project> getExistingProjects() {
        try {
            return getExistingProjectsNotSafe();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Project> getExistingProjectsNotSafe() throws IOException, InterruptedException {
        String path
                = "/api/v2025.8/projects?include-deleting=false&include-reanalyzing=false";

        HttpResponse<String> response = httpHelperTeamscale.executeGetRequest(path);

        return List.of(new ObjectMapper().readValue(response.body(), Project[].class));
    }
}
