package io.teamscale.api;

import io.teamscale.api.model.project.Project;
import jakarta.inject.Inject;
import model.Proof;
import util.Constants;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
                        "Included file names":"%s",
                        "Excluded file names":"%s",
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
                     "profile":"Java (default)"
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

    private final ProjectsFetcher projectsFetcher;
    private final HttpHelperTeamscale httpHelperTeamscale;

    @Inject
    public ProjectCreator(ProjectsFetcher projectsFetcher, HttpHelperTeamscale httpHelperTeamscale) {
        this.projectsFetcher = projectsFetcher;
        this.httpHelperTeamscale = httpHelperTeamscale;
    }

    public void createProjectsWithProofs(List<Proof> proofs, int numberOfProjectsToBeCreated) {
        System.out.println("Getting existing projects from teamscale");
        Set<String> existingProjectNames = projectsFetcher.getExistingProjects().stream().map(Project::getName).collect(Collectors.toSet());

        System.out.println("Creating projects");
        int i = 0;
        for (Proof proof : proofs) {
            
            if (proof.language() != Constants.CURRENT_LANGUAGE) {
                throw new RuntimeException("Language was not expected");
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

        System.out.println();
        System.out.println("Finished");
    }

    private void createProjectWithProof(Proof proof) {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(proof.analysisStartTime() * 1000L);
        String startData = formatter.format(calendar.getTime());

        calendar.setTimeInMillis(proof.analysisEndTime() * 1000L);
        String endDate = formatter.format(calendar.getTime());

        try {
            createSingleProject(proof.repoOwner(), proof.repoName(), proof.defaultBranch(), startData, endDate);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createSingleProject(String repoOwner, String repoName, String defaultBranch, String startData, String endDate) throws IOException, InterruptedException {
        //Build request body
        String suffix = repoOwner + "/" + repoName;
        String projectName = repoOwner + "." + repoName;
        String request = REQUEST.formatted(projectName, Constants.CURRENT_LANGUAGE.getIncludedFileNames(), Constants.CURRENT_LANGUAGE.getExcludedFileNames(), suffix, defaultBranch, defaultBranch, startData, endDate, projectName);

        String path = "/api/v2025.8/projects?skip-project-validation=false";
        HttpResponse<String> response = httpHelperTeamscale.executePostRequest(path, request);

        System.out.println("Status Code: " + response.statusCode());
    }
}
