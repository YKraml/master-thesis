package io.git.graphql.model.issues;

import io.git.graphql.model.common.PageInfo;

import java.util.List;

public record Issues(PageInfo pageInfo, List<Issue> nodes) {

}
