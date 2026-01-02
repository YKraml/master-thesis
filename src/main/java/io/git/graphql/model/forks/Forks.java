package io.git.graphql.model.forks;

import io.git.graphql.model.common.PageInfo;

import java.util.List;

public record Forks(PageInfo pageInfo, List<Edge> edges) {

}
