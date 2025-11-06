package teamscale;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.Constants;
import common.Result;
import common.ResultReaderWriter;
import jakarta.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import teamscale.model.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectCreator {

    private static final String REQUEST = """
            {
               "name":"%s",
               "connectors":[
                  {
                     "type":"Git",
                     "connectorIdentifierOptionName":"Repository identifier",
                     "options":{
                        "Repository identifier":"git",
                        "Included file names":"**.java",
                        "Excluded file names":"**/package-info.java, **/module-info.java",
                        "Account":"kraml",
                        "Path suffix":"%s",
                        "Default branch name":"%s",
                        "Enable branch analysis":"false",
                        "Included branches":"%s",
                        "Excluded branches":"",
                        "Start revision":"",
                        "Content exclude":"",
                        "Polling interval":"60",
                        "Include files stored via Git LFS":"false",
                        "Important Branches":"",
                        "Include Submodules":"false",
                        "Submodule recursion depth":"10",
                        "SSH Private Key ID":"",
                        "Test-code path pattern":"**/test/**, **/testFixtures/**",
                        "Test-code path exclude pattern":"",
                        "Prepend repository identifier":"false",
                        "End revision":"",
                        "Text filter":"",
                        "Language mapping":"",
                        "Analysis report mapping":"",
                        "Partition Pattern":"",
                        "File-size exclude":"1MB",
                        "Source library connector":"false",
                        "Run to exhaustion":"false",
                        "Preserve empty commits":"true",
                        "Delta size":"500",
                        "Path prefix transformation":"",
                        "Path transformation":"",
                        "Encoding":"",
                        "Author transformation":"",
                        "Branch transformation":"",
                        "Fetch additional ref specs":"",
                        "Skip Voting on merge requests without relevant changes":"false"
                     }
                  }
               ],
               "codeScopes":[
                  {
                     "name":"default",
                     "pathIncludes":[
                       \s
                     ],
                     "pathExcludes":[
                       \s
                     ],
                     "contentIncludes":[
                       \s
                     ],
                     "contentExcludes":[
                       \s
                     ],
                     "profile":"master-thesis-analysis-profile"
                  }
               ],
               "branchingConfiguration":{
                  "branchConfigurations":[
                     {
                        "branchNamePattern":".*",
                        startDate: "2000-01-01"
                     }
                  ]
               },
               "publicIds":[
                  "%s"
               ]
            }""";

    @Inject
    public ProjectCreator() {
    }

    public void createAllProjects() throws IOException {
        List<Result> results = new ResultReaderWriter().readAllResults();

        List<Project> existingProjects = getExistingProjects();
        Set<String> existingProjectNames = existingProjects.stream().map(Project::getName).collect(Collectors.toSet());

        for (Result result : results) {
            if (result.linters().isEmpty()) {
                continue;
            }

            String projectName = result.repoOwner() + "." + result.repoName();
            String suffix = result.repoOwner() + "/" + result.repoName();
            String defaultBranch = result.defaultBranch();

            System.out.println();
            if (existingProjectNames.contains(projectName)) {
                System.out.println("Skipped " + result.link());
            } else {
                System.out.println("Create: " + result.link());
                createSingleProject(projectName, suffix, defaultBranch);
                System.out.println("Finished creation");
            }
        }
    }

    public List<Project> getExistingProjects() {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("https://thesis-yannick.teamscale.dev/api/v2025.8/projects?include-deleting=false&include-reanalyzing=false");

            // Authentication
            addAuthTo(httpGet);

            // Headers
            httpGet.setHeader("accept", "application/json");

            // Execute the POST request
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("Response Code: " + statusCode);

                HttpEntity entity = response.getEntity();
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
            e.printStackTrace();
        }

        return List.of();
    }

    private void deleteSingleProject(String project_name) {
        //TODO
    }

    private void createSingleProject(String project_name, String suffix, String defaultBranch) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httppost = new HttpPost("https://thesis-yannick.teamscale.dev/api/v2025.8/projects?skip-project-validation=false");

            // Authentication
            addAuthTo(httppost);

            // Headers
            httppost.setHeader("accept", "*/*");
            httppost.setHeader("Content-Type", "application/json");

            // Body
            String request = REQUEST.formatted(project_name, suffix, defaultBranch, defaultBranch, project_name);
            StringEntity requestEntity = new StringEntity(request, StandardCharsets.UTF_8);
            httppost.setEntity(requestEntity);

            // Execute the POST request
            try (CloseableHttpResponse response = httpclient.execute(httppost)) {
                System.out.println("Response Code: " + response.getStatusLine().getStatusCode());

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new IOException("Empty response entity");
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                    reader.lines().forEach(System.out::println);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addAuthTo(HttpRequestBase request) {
        String auth = "kraml:" + Constants.TEAMSCALE_ACCESS_KEY;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        request.setHeader("Authorization", authHeader);
    }
}
