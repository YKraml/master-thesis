package api.graphql.model.issues;

import api.graphql.model.common.PageInfo;

import java.util.List;

public record Issues(PageInfo pageInfo, List<Issue> nodes) {

}
