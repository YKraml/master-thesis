package api.graphql.model.forks;

import api.graphql.model.common.PageInfo;

import java.util.List;

public record Forks(PageInfo pageInfo, List<Edge> edges) {

}
