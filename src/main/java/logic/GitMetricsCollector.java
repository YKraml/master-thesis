package logic;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.RepositoryDownloader;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GitHub;
import util.Constants;
import util.GuiceModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GitMetricsCollector {

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new GuiceModule());
        GitMetricsCollector gitMetricsCollector = injector.getInstance(GitMetricsCollector.class);
        GitMetrics gitMetrics = gitMetricsCollector.collectGitMetrics("AntennaPod", "AntennaPod");

        System.out.println(gitMetrics.toString());
    }

    private final GitHub gitHub;
    private final RepositoryDownloader repositoryDownloader;

    @Inject
    public GitMetricsCollector(GitHub gitHub, RepositoryDownloader repositoryDownloader) {
        this.gitHub = gitHub;
        this.repositoryDownloader = repositoryDownloader;

    }

    public GitMetrics collectGitMetrics(String repoOwner, String repoName) throws GitAPIException, IOException, InterruptedException {
        // Git gitRepo = repositoryDownloader.getRepo(repoOwner, repoName);
        // GHRepository ghRepo = gitHub.getRepository(repoOwner + "/" + repoName);

        //GraphQLSchema schema = AnnotationsSchemaCreator
        //        .newAnnotationsSchema()
        //        .query(Query.class)
        //        .build();
        //GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        System.out.println(gitHub.getRateLimit());

        String query = """
                query {
                  repository(owner:"AntennaPod", name:"AntennaPod") {
                    owner {
                        login
                    }
                    name
                    issues(last:1) {
                      edges {
                        node {
                          title
                          createdAt
                        }
                      }
                    }
                  }
                }""";

        String formattedQuery = query
                .replace(" ", "")
                .replace("\n", " ")
                .replace("\r", "")
                .replace("\"", "\\\"");

        String json = """
                {"query": "{QUERY}"}
                """.replace("{QUERY}", formattedQuery);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/graphql"))
                .header("Authorization", "Bearer " + Constants.GITHUB_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());

        return new GitMetrics();
    }

    public class GitMetrics {

    }

}


