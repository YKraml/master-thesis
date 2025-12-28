package api.graphql;

import api.graphql.model.common.PageInfo;
import api.graphql.model.stars.Star;
import api.graphql.model.stars.StarRoot;
import jakarta.inject.Inject;
import logic.StateHandler;

import java.net.http.HttpClient;
import java.util.stream.Stream;

public class StarFetcher extends GraphQlFetcher<StarRoot, Star> {

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

    @Inject
    public StarFetcher(StateHandler stateHandler, HttpClient httpClient) {
        super(StarRoot.class, stateHandler, httpClient);
    }

    @Override
    public String getQuery() {
        return QUERY;
    }

    @Override
    protected Stream<Star> getStream(StarRoot issueRoot) {
        return issueRoot.data().repository().stargazers().edges().stream();
    }

    @Override
    protected PageInfo getPageInfo(StarRoot issueRoot) {
        return issueRoot.data().repository().stargazers().pageInfo();
    }
}