package graphql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.model.common.PageInfo;
import graphql.model.stars.Star;
import graphql.model.stars.StarRoot;
import jakarta.inject.Inject;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class StarFetcher {

    private static final String QUERY = """
            query ($owner: String!, $name: String!, $pageSize: Int!, $cursor: String) {
              repository(owner: $owner, name: $name) {
                stargazers(
                  first: $pageSize,
                  after: $cursor,
                  orderBy: { field: STARRED_AT, direction: ASC }
                ) {
                  pageInfo {
                    hasNextPage
                    endCursor
                  }
                  edges {
                    starredAt
                  }
                }
              }
            }
            """;

    private final HttpHelperGraphQl httpHelperGraphQl;

    @Inject
    public StarFetcher(HttpHelperGraphQl httpHelperGraphQl) {
        this.httpHelperGraphQl = httpHelperGraphQl;
    }

    public List<Star> getStarsOf(String owner, String repo) {
        try {
            return getStarsOfNonSafe(owner, repo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Star> getStarsOfNonSafe(String owner, String repo) throws Exception {
        List<StarRoot> allStarsRoot = new ArrayList<>();

        String cursor = null;
        boolean hasNextPage = true;
        while (hasNextPage) {
            String requestBody = buildRequestBody(owner, repo, cursor);
            HttpResponse<String> response = httpHelperGraphQl.getResponse(requestBody);
            StarRoot issueRoot = new ObjectMapper().readValue(response.body(), StarRoot.class);

            allStarsRoot.add(issueRoot);
            PageInfo pageInfo = issueRoot.data().repository().stargazers().pageInfo();
            hasNextPage = pageInfo.hasNextPage();
            cursor = pageInfo.endCursor();
        }

        return allStarsRoot.stream().flatMap(issueRoot -> issueRoot.data().repository().stargazers().edges().stream()).toList();
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
