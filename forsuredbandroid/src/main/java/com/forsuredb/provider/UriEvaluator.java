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
package com.forsuredb.provider;

import android.net.Uri;

import com.forsuredb.ForSureAndroidInfoFactory;
import com.fsryan.forsuredb.api.FSJoin;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *     A utility class to provide information about {@link Uri}s used to describe either tables
 *     or records in tables.
 * </p>
 * @author Ryan Scott
 */
public class UriEvaluator {

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
    public static boolean isJoin(Uri uri) {
        for (FSJoin.Type type : FSJoin.Type.values()) {
            try {
                if (!Strings.isNullOrEmpty(uri.getQueryParameter(UriJoiner.joinMap.get(type)))) {
                    return true;
                }
            } catch (Exception e) {}
        }
        return false;
    }
}
