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
package com.fsryan.forsuredb.provider;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.fsryan.forsuredb.ForSureAndroidInfoFactory;
import com.fsryan.forsuredb.api.FSJoin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 *     A utility class to provide information about {@link Uri}s used to describe either tables
 *     or records in tables.
 * </p>
 * @author Ryan Scott
 */
public class UriEvaluator {

    public static final String DISTINCT_QUERY_PARAM = "DISTINCT";
    public static final String LAST_QUERY_PARAM = "LAST";
    public static final String FIRST_QUERY_PARAM = "FIRST";
    public static final String OFFSET_QUERY_PARAM = "OFFSET";
    public static final String ORDER_BY_QUERY_PARM = "ORDER_BY";

    /**
     * TODO: this is probably not correct, but it doesn't seem broken for the way {@link Uri} is being used
     * <p>
     *     A {@link Uri} is considered to be a specific record {@link Uri} in the case that it:
     *     <ul>
     *         <li>has less than 2 path segments</li>
     *         <li>has an odd number of path segments (such as parent/1/child)</li>
     *     </ul>
     * </p>
     * @param uri The {@link Uri} to check
     * @return true if the Uri is a specific record Uri, false if not
     */
    public static boolean isSpecificRecordUri(Uri uri) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() < 2 || pathSegments.size() % 2 == 1) {
            return false;
        }

        return true;
    }

    public static List<Uri> tableReferences(Uri uri) {
        if (uri == null) {
            return null;
        }

        List<Uri> tableUris = new ArrayList<>();
        for (String joinedTable : new JoinUriParser(uri).getJoinedTableNames()) {
            tableUris.add(ForSureAndroidInfoFactory.inst().tableResource(joinedTable));
        }
        return tableUris;
    }

    /**
     * <p>
     *     A {@link Uri} is a join if it contains any of the following query parameters:
     *     <ul>
     *         <li>INNER JOIN</li>
     *         <li>OUTER JOIN</li>
     *         <li>CROSS JOIN</li>
     *         <li>LEFT JOIN</li>
     *         <li>NATURAL JOIN</li>
     *     </ul>
     * </p>
     * @param uri The {@link Uri} to evaluate
     * @return true if the {@link Uri} is a join; false otherwise
     */
    public static boolean isJoin(@NonNull Uri uri) {
        for (FSJoin.Type type : FSJoin.Type.values()) {
            try {
                String join = uri.getQueryParameter(UriJoiner.joinMap.get(type));
                if (join != null && !join.isEmpty()) {
                    return true;
                }
            } catch (RuntimeException e) {

            }
        }
        return false;
    }

    public static boolean hasFirstOrLastParam(@NonNull Uri uri) {
        final Set<String> names = uri.getQueryParameterNames();
        return names.contains(FIRST_QUERY_PARAM)
                || names.contains(LAST_QUERY_PARAM)
                || names.contains(OFFSET_QUERY_PARAM);
    }

    public static int offsetFrom(@NonNull Uri uri) {
        String offset = uri.getQueryParameter(OFFSET_QUERY_PARAM);
        return offset == null ? 0 : Integer.valueOf(offset);
    }

    public static int limitFrom(@NonNull Uri uri) {
        String offset = uri.getQueryParameter(FIRST_QUERY_PARAM);
        offset = offset == null ? uri.getQueryParameter(LAST_QUERY_PARAM) : offset;
        return offset == null ? 0 : Integer.valueOf(offset);
    }

    public static boolean offsetFromLast(@NonNull Uri uri) {
        String offset = uri.getQueryParameter(FIRST_QUERY_PARAM);
        int frontOffset = offset == null ? 0 : Integer.valueOf(offset);
        return frontOffset <= 0 && limitFrom(uri) > 0;
    }

    @NonNull
    public static String orderingFrom(@NonNull Uri uri) {
        String sort = uri.getQueryParameter(ORDER_BY_QUERY_PARM);
        return sort == null ? "" : sort;
    }
}
