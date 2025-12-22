package graphql.model.stars;

import graphql.model.common.PageInfo;

import java.util.List;

public record Stargazers(PageInfo pageInfo, List<Star> edges) {

}
