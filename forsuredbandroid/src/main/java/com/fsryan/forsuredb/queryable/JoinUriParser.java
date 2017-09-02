package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import java.util.HashSet;
import java.util.Set;

/*package*/ class JoinUriParser {

    private final Uri uri;
    private final String baseTableName;

    public JoinUriParser(Uri uri) {
        this.uri = uri;
        baseTableName = uri == null ? null : uri.getPathSegments().get(0);
    }

    public Set<String> getJoinedTableNames() {
        final Set<String> ret = new HashSet<>();
        if (baseTableName != null && !baseTableName.isEmpty()) {
            ret.add(baseTableName);
        }
        if (uri == null || uri.getQuery() == null || uri.getQuery().isEmpty()) {
            return ret;
        }

        final String[] parsedQuery = uri.getQuery().split("&");
        for (String query : parsedQuery) {
            final String tableName = joinedTableNameFromIndividualQuery(query);
            if (tableName != null && !tableName.isEmpty()) {
                ret.add(tableName);
            }
        }

        return ret;
    }

    private String joinedTableNameFromIndividualQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        final String[] splitQuery = query.split("=");
        if (splitQuery.length < 2) {
            return null;
        }

        return joinedTableNameFromRhs(splitQuery[1]);
    }

    private String joinedTableNameFromRhs(String queryRhs) {
        final String[] splitRhs = queryRhs.split(" ");
        if (splitRhs.length == 0) {
            return null;
        }

        return splitRhs[0];
    }
}
