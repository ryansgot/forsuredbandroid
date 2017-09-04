/*
   forsuredbandroid, an android companion to the forsuredb project

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.fsryan.forsuredb.queryable;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.fsryan.forsuredb.api.FSJoin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fsryan.forsuredb.queryable.UriEvaluator.isSpecificRecordUri;

/**
 * <p>
 *     Utility class for encoding joins onto {@link Uri} objects and reading joins off of them.
 *     This facilitates the ability to automatically create joins as resources and query the
 *     {@link FSDefaultProvider} with a join {@link Uri} in the same way that you query it
 *     without a join {@link Uri}.
 * </p>
 * @author Ryan Scott
 */
public class UriJoinTranslator {

    /*package*/ static final Map<FSJoin.Type, String> joinMap = new HashMap<>();
    static {
        for (FSJoin.Type type : FSJoin.Type.values()) {
            joinMap.put(type, type.toString() + " JOIN");
        }
    }

    /**
     * @param uri The {@link Uri} to which you wish to add joins
     * @param baseTableName The name of the table you're querying
     * @param joins A {@link List} of {@link FSJoin} that you want to add to the uri
     * @return A {@link Uri} with added query parameters for each {@link FSJoin} passed in
     */
    public static Uri join(Uri uri, String baseTableName, List<FSJoin> joins) {
        if (uri == null || joins == null) {
            return uri;
        }

        Uri.Builder ub = new Uri.Builder().scheme(uri.getScheme()).authority(uri.getAuthority());
        for (String pathSegment : uri.getPathSegments()) {
            ub.appendPath(pathSegment);
        }

        Set<String> joinedTables = new HashSet<>();
        joinedTables.add(baseTableName);
        for (FSJoin join : joins) {
            Pair<String, String> tableJoinTextPair = joinTextFrom(join, joinedTables);
            if (tableJoinTextPair == null) {
                Log.w(UriJoinTranslator.class.getSimpleName(), "Cannot join " + join.getParentTable() + " and " + join.getChildTable() + " because both tables are already joined in this query");
                continue;
            }
            joinedTables.add(tableJoinTextPair.first);
            ub.appendQueryParameter(joinMap.get(join.getType()), tableJoinTextPair.second);
        }

        return ub.build();
    }

    /**
     * @param uri The {@link Uri} from which you would like to get the join string
     * @return The join (FROM) part of the query without a prefixed "FROM"
     */
    @NonNull
    public static String joinStringFrom(@NonNull Uri uri) {
        final String baseTable = isSpecificRecordUri(uri) ? uri.getPathSegments().get(uri.getPathSegments().size() - 2) : uri.getLastPathSegment();
        StringBuffer sb = new StringBuffer(baseTable);


        String query = uri.getQuery();
        if (query == null) {
            return "";
        }

        int start = 0;
        do {
            int nextAmpersand = query.indexOf('&', start);
            int end = nextAmpersand != -1 ? nextAmpersand : query.length();

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            // TODO: make this faster
            final String key = query.substring(start, separator);
            final String value = separator == end ? "" : query.substring(separator + 1, end);
            if (joinMap.containsValue(key)) {
                appendJoinStringTo(sb, key, value);
            }

            // Move start to end of name.
            if (nextAmpersand != -1) {
                start = nextAmpersand + 1;
            } else {
                break;
            }
        } while (true);

        return sb.toString();
    }

    private static void appendJoinStringTo(StringBuffer sb, String joinType, String joinQuery) {
        if (joinQuery == null || joinQuery.isEmpty()) {
            return;
        }
        sb.append(" ").append(joinType).append(" ").append(joinQuery);
    }

    private static Pair<String, String> joinTextFrom(FSJoin join, Set<String> joinedTables) {
        if (join.getType() == FSJoin.Type.NATURAL) {
            return Pair.create("", "");
        }
        String tableToJoin = tableToJoin(join, joinedTables);
        if (tableToJoin == null) {
            return null;
        }
        final StringBuilder joinBuf = new StringBuilder(tableToJoin).append(" ON ");
        for (Map.Entry<String, String> childToParentColumnMapEntry : join.getChildToParentColumnMap().entrySet()) {
            joinBuf.append(join.getParentTable()).append("").append(childToParentColumnMapEntry.getValue())
                    .append(" = ")
                    .append(join.getChildTable()).append("").append(childToParentColumnMapEntry.getKey())
                    .append(" AND ");
        }
        return Pair.create(tableToJoin, joinBuf.delete(joinBuf.length() - 5, joinBuf.length()).toString());
    }

    private static String tableToJoin(FSJoin join, Set<String> joinedTables) {
        return joinedTables.contains(join.getParentTable())
                ? joinedTables.contains(join.getChildTable()) ? null : join.getChildTable()
                : join.getParentTable();
    }
}
