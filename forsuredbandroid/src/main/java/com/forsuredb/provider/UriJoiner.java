/*
   forsuredb, an object relational mapping tool

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
package com.forsuredb.provider;

import android.net.Uri;

import com.forsuredb.api.FSJoin;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     Utility class for encoding joins onto {@link Uri} objects and reading joins off of them.
 *     This facilitates the ability to automatically create joins as resources and query the
 *     {@link FSDefaultProvider} with a join {@link Uri} in the same way that you query it
 *     without a join {@link Uri}.
 * </p>
 * @author Ryan Scott
 */
public class UriJoiner {

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
        for (FSJoin join : joins) {
            ub.appendQueryParameter(joinMap.get(join.type()), joinTextFrom(join, baseTableName));
        }

        return ub.build();
    }

    /**
     * @param uri The {@link Uri} from which you would like to get the join string
     * @return The join (FROM) part of the query without a prefixed "FROM"
     */
    public static String joinStringFrom(Uri uri) {
        final String baseTable = UriEvaluator.isSpecificRecordUri(uri) ? uri.getPathSegments().get(uri.getPathSegments().size() - 2) : uri.getLastPathSegment();
        StringBuffer sb = new StringBuffer(baseTable);
        for (String key : uri.getQueryParameterNames()) {
            if (!joinMap.containsValue(key)) {
                continue;
            }
            for (String queryParameter : getQueryParameters(uri, key)) {
                appendJoinStringTo(sb, key, queryParameter);
            }

        }
        return sb.toString();
    }

    private static void appendJoinStringTo(StringBuffer sb, String joinType, String joinQuery) {
        if (Strings.isNullOrEmpty(joinQuery)) {
            return;
        }
        sb.append(" ").append(joinType).append(" ").append(joinQuery);
    }

    private static String joinTextFrom(FSJoin join, String baseTable) {
        if (join.type() == FSJoin.Type.NATURAL) {
            return "";
        }
        return tableToJoin(join, baseTable) + " ON " + join.parentTable() + "." + join.parentColumn() + " = " + join.childTable() + "." + join.childColumn();
    }

    private static String tableToJoin(FSJoin join, String baseTable) {
        return baseTable.equals(join.childTable()) ? join.parentTable() : join.childTable();
    }

    /**
     * TODO: deprecate this and figure out how to use the Uri class correctly
     * This is a copy-paste and slight change from the {@link Uri#getQueryParameters(String)}
     * method in the Uri class. I did this because the existing method appears to not work
     * correctly for this purpose, but, with the slight change below, it works.
     */
    private static List<String> getQueryParameters(Uri uri, String key) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        String query = uri.getQuery();
        if (query == null) {
            return Collections.emptyList();
        }

        ArrayList<String> values = new ArrayList<String>();

        int start = 0;
        do {
            int nextAmpersand = query.indexOf('&', start);
            int end = nextAmpersand != -1 ? nextAmpersand : query.length();

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            if (separator - start == key.length()
                    && query.regionMatches(start, key, 0, key.length())) {
                if (separator == end) {
                    values.add("");
                } else {
                    values.add(query.substring(separator + 1, end));
                }
            }

            // Move start to end of name.
            if (nextAmpersand != -1) {
                start = nextAmpersand + 1;
            } else {
                break;
            }
        } while (true);

        return Collections.unmodifiableList(values);
    }
}
