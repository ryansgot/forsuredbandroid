package com.fsryan.fosuredb.provider;

import android.net.Uri;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

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
        if (uri == null || Strings.isNullOrEmpty(uri.getQuery()) || Strings.isNullOrEmpty(baseTableName)) {
            return Sets.newHashSet(baseTableName);
        }

        final String[] parsedQuery = uri.getQuery().split("&");
        final Set<String> retSet = new HashSet<>();
        retSet.add(baseTableName);
        for (String query : parsedQuery) {
            final String tableName = joinedTableNameFromIndividualQuery(query);
            if (!Strings.isNullOrEmpty(tableName) && !retSet.contains(tableName)) {
                retSet.add(tableName);
            }
        }

        return retSet;
    }

    private String joinedTableNameFromIndividualQuery(String query) {
        if (Strings.isNullOrEmpty(query)) {
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
