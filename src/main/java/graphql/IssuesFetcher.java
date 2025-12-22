package graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.model.common.PageInfo;
import graphql.model.issues.IssueRoot;
import graphql.model.issues.Issue;
import jakarta.inject.Inject;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class IssuesFetcher {

    private static final String QUERY = """
            query ($owner: String!, $name: String!, $pageSize: Int!, $cursor: String) {
              repository(owner: $owner, name: $name) {
                issues(
                  first: $pageSize,
                  after: $cursor,
                  orderBy: { field: CREATED_AT, direction: ASC }
                ) {
                  pageInfo {
                    hasNextPage
                    endCursor
                  }
                  nodes {
                    number
                    title
                    createdAt
                    state
                  }
                }
              }
            }
            """;

    private final HttpHelperGraphQl httpHelperGraphQl;

    @Inject
    public IssuesFetcher(HttpHelperGraphQl httpHelperGraphQl) {
        this.httpHelperGraphQl = httpHelperGraphQl;
    }

    public List<Issue> getIssuesOf(String owner, String repo) {
        try {
            return getIssuesOfNonSafe(owner, repo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Issue> getIssuesOfNonSafe(String owner, String repo) throws Exception {
        List<IssueRoot> allIssueRoots = new ArrayList<>();

        String cursor = null;
        boolean hasNextPage = true;
        while (hasNextPage) {
            String requestBody = buildRequestBody(owner, repo, cursor);
            HttpResponse<String> response = httpHelperGraphQl.getResponse(requestBody);
            IssueRoot issueRoot = new ObjectMapper().readValue(response.body(), IssueRoot.class);

            allIssueRoots.add(issueRoot);
            PageInfo pageInfo = issueRoot.data().repository().issues().pageInfo();
            hasNextPage = pageInfo.hasNextPage();
            cursor = pageInfo.endCursor();
        }

        return allIssueRoots.stream().flatMap(issueRoot -> issueRoot.data().repository().issues().nodes().stream()).toList();
    }

    private static String buildRequestBody(String owner, String repo, String cursor) throws Exception {
        ObjectNode variables = new ObjectMapper().createObjectNode()
                .put("owner", owner)
                .put("name", repo)
                .put("pageSize", 100)
                .put("cursor", cursor); //cursor can be null

        JsonNode root = new ObjectMapper().createObjectNode()
                .put("query", QUERY)
                .set("variables", variables);

        return new ObjectMapper().writeValueAsString(root);
    }
}
