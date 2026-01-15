package io.teamscale.api;

import jakarta.inject.Inject;
import model.CemProject;
import util.Constants;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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

    private final HttpHelperTeamscale httpHelperTeamscale;

    @Inject
    public ProjectCreator(HttpHelperTeamscale httpHelperTeamscale) {
        this.httpHelperTeamscale = httpHelperTeamscale;
    }

    public void createProjectWithCemProject(CemProject cemProject) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(cemProject.analysisStartTime() * 1000L);
        String startData = formatter.format(calendar.getTime());

        calendar.setTimeInMillis(cemProject.analysisEndTime() * 1000L);
        String endDate = formatter.format(calendar.getTime());

        try {
            createSingleProject(cemProject.repoOwner(), cemProject.repoName(), cemProject.defaultBranch(), startData, endDate);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createSingleProject(String repoOwner, String repoName, String defaultBranch, String startData, String endDate) throws IOException, InterruptedException {
        //Build request body
        String suffix = repoOwner + "/" + repoName;
        String projectName = repoOwner + "." + repoName;
        String request = REQUEST.formatted(
                projectName,
                Constants.CURRENT_LANGUAGE.getIncludedFileNames(),
                Constants.CURRENT_LANGUAGE.getExcludedFileNames(),
                suffix,
                defaultBranch,
                defaultBranch,
                startData,
                endDate,
                projectName
        );

        String path = "/api/v2025.8/projects?skip-project-validation=false";

        HttpResponse<String> response = httpHelperTeamscale.executePostRequest(path, request);
        System.out.println("Status Code: " + response.statusCode());
    }
}
