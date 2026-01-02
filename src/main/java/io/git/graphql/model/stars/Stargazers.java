package io.git.graphql.model.stars;

import io.git.graphql.model.common.PageInfo;

import java.util.List;

public record Stargazers(PageInfo pageInfo, List<Star> edges) {

}
