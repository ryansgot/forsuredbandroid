package com.fsryan.forsuredb.provider;

import android.net.Uri;

import java.util.HashSet;
import java.util.Set;

/*package*/ class JoinUriParser {

    private final Uri uri;
    private final String baseTableName;

    public JoinUriParser(Uri uri) {
        this.uri = uri;
        baseTableName = getBaseTableName();
    }

    public Set<String> getJoinedTableNames() {
        if (uri == null || uri.getQuery() == null || uri.getQuery().isEmpty() || baseTableName == null || baseTableName.isEmpty()) {
            Set<String> ret = new HashSet<>();
            ret.add(baseTableName);
            return ret;
        }

        final String[] parsedQuery = uri.getQuery().split("&");
        final Set<String> retSet = new HashSet<>();
        retSet.add(baseTableName);
        for (String query : parsedQuery) {
            final String tableName = joinedTableNameFromIndividualQuery(query);
            if (tableName != null && !tableName.isEmpty() && !retSet.contains(tableName)) {
                retSet.add(tableName);
            }
        }

        return retSet;
    }

    private String joinedTableNameFromIndividualQuery(String query) {
        if (query != null && !query.isEmpty()) {
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

    private String getBaseTableName() {
        if (uri == null) {
            return null;
        }
        return uri.getPathSegments().get(0);
    }
}
