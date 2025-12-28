package api.graphql;

import api.graphql.model.common.PageInfo;
import api.graphql.model.forks.Edge;
import api.graphql.model.forks.Fork;
import api.graphql.model.forks.ForkRoot;
import jakarta.inject.Inject;
import logic.StateHandler;

import java.net.http.HttpClient;
import java.util.stream.Stream;

public class ForkFetcher extends GraphQlFetcher<ForkRoot, Fork> {

    private static final String QUERY = """
            query ($owner: String!, $name: String!, $pageSize: Int!, $cursor: String) {
              repository(owner: $owner, name: $name) {
                forks(
                  first: $pageSize,
                  after: $cursor,
                  orderBy: { field: CREATED_AT, direction: ASC }
                ) {
                  pageInfo {
                    hasNextPage
                    endCursor
                  }
                  edges {
                    node {
                      createdAt
                    }
                  }
                }
              }
            }
            """;

    @Inject
    protected ForkFetcher(StateHandler stateHandler, HttpClient httpClient) {
        super(ForkRoot.class, stateHandler, httpClient);
    }

    @Override
    protected String getQuery() {
        return QUERY;
    }

    @Override
    protected Stream<Fork> getStream(ForkRoot forkRoot) {
        return forkRoot.data().repository().forks().edges().stream().map(Edge::node);
    }

    @Override
    protected PageInfo getPageInfo(ForkRoot forkRoot) {
        return forkRoot.data().repository().forks().pageInfo();
    }
}
