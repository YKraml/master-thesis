package api.graphql.model.stars;

import api.graphql.model.common.PageInfo;

import java.util.List;

public record Stargazers(PageInfo pageInfo, List<Star> edges) {

}
