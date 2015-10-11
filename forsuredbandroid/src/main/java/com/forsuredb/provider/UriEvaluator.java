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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     A utility class to provide information about {@link Uri}s used to describe either tables
 *     or records in tables.
 * </p>
 */
public class UriEvaluator {

    /**
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
