package api;

import util.Constants;
import model.Proof;
import io.ProofReaderWriter;
import jakarta.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import api.model.project.Project;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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
                        "Start revision":"%s",
                        "Content exclude":"",
                        "Polling interval":"3600",
                        "Include files stored via Git LFS":"false",
                        "Important Branches":"",
                        "Include Submodules":"false",
                        "Submodule recursion depth":"10",
                        "SSH Private Key ID":"",
                        "Test-code path pattern":"**/test/**, **/testFixtures/**",
                        "Test-code path exclude pattern":"",
                        "Prepend repository identifier":"false",
                        "End revision":"%s",
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
                     "pathIncludes":[],
                     "pathExcludes":[],
                     "contentIncludes":[],
                     "contentExcludes":[],
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

    private final ProjectsGetter projectsGetter;

    @Inject
    public ProjectCreator(ProjectsGetter projectsGetter) {
        this.projectsGetter = projectsGetter;
    }

    public void createAllProjects() throws IOException {
        System.out.println("Start reading proof files");
        List<Proof> proofs = new ProofReaderWriter().readAllProofs();
        proofs.sort(Comparator.comparingInt(Proof::numberOfStars));

        System.out.println("Getting existing projects from teamscale");
        List<Project> existingProjects = projectsGetter.getExistingProjects();
        Set<String> existingProjectNames = existingProjects.stream().map(Project::getName).collect(Collectors.toSet());

        System.out.println("Creating projects");
        createProjects(proofs, existingProjectNames);

        System.out.println();
        System.out.println("Finished");
    }

    private void createProjects(List<Proof> proofs, Set<String> existingProjectNames) {
        int numberOfProjectsToBeCreated = getNumberOfProjectsToBeCreated();
        int i = 0;
        for (Proof proof : proofs) {
            if (proof.linters().isEmpty()) {
                continue;
            }

            System.out.println();
            if (existingProjectNames.contains(proof.repoOwner() + "." + proof.repoName())) {
                System.out.println("Skipped " + proof.link());
            } else {
                if (i == numberOfProjectsToBeCreated) {
                    break;
                }
                i++;

                System.out.println(i + ". Create: " + proof.link());
                createProjectWithProof(proof);
                System.out.println("Finished creation");
            }
        }
    }

    private static int getNumberOfProjectsToBeCreated() {
        JFrame topFrame = new JFrame();
        topFrame.setAlwaysOnTop(true);
        topFrame.setUndecorated(true);
        topFrame.setLocationRelativeTo(null); // center on screen
        topFrame.setVisible(true);

        String input = JOptionPane.showInputDialog(
                topFrame,
                "How many projects should be created?"
        );

        topFrame.dispose(); // remove temp frame

        int numberOfProjectsToBeCreeated;
        try {
            numberOfProjectsToBeCreeated = Integer.parseInt(input);
        } catch (Exception e) {
            numberOfProjectsToBeCreeated = -1;
        }
        return numberOfProjectsToBeCreeated;
    }

    public void createProjectWithProof(Proof proof) {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(proof.analysisStartTime() * 1000L);
        String startData = formatter.format(calendar.getTime());

        calendar.setTimeInMillis(proof.analysisEndTime() * 1000L);
        String endDate = formatter.format(calendar.getTime());

        createSingleProject(proof.repoOwner(), proof.repoName(), proof.defaultBranch(), startData, endDate);
    }

    private void createSingleProject(String repoOwner, String repoName, String defaultBranch, String startData, String endDate) {
        String suffix = repoOwner + "/" + repoName;
        String projectName = repoOwner + "." + repoName;

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httppost = new HttpPost("https://thesis-yannick.teamscale.dev/api/v2025.8/projects?skip-project-validation=false");

            // Authentication
            addAuth(httppost);

            // Headers
            httppost.setHeader("accept", "*/*");
            httppost.setHeader("Content-Type", "application/json");

            // Body
            String request = REQUEST.formatted(projectName, suffix, defaultBranch, defaultBranch, startData, endDate, projectName);
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

    public static void addAuth(HttpRequestBase request) {
        String auth = "kraml:" + Constants.TEAMSCALE_ACCESS_KEY;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        request.setHeader("Authorization", authHeader);
    }
}
