package api.teamscale;

import api.teamscale.model.project.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import util.Constants;

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
        String url = Constants.TEAMSCALE_URL
                + "/api/v2025.8/projects?include-deleting=false&include-reanalyzing=false";

        HttpResponse<String> response = httpHelperTeamscale.getResponse(url);

        return List.of(new ObjectMapper().readValue(response.body(), Project[].class));
    }
}
