package graphql.model.issues;

import graphql.model.common.PageInfo;

import java.util.List;

public record Issues(PageInfo pageInfo, List<Issue> nodes) {

}
