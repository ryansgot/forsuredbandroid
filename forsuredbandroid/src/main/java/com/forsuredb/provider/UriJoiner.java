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

import com.forsuredb.ForSureAndroidInfoFactory;
import com.forsuredb.api.FSJoin;
import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UriJoiner {

    /*package*/ static final Map<FSJoin.Type, String> joinMap = new HashMap<>();
    static {
        for (FSJoin.Type type : FSJoin.Type.values()) {
            joinMap.put(type, type.toString() + " JOIN");
        }
    }

    public static Uri join(Uri uri, List<FSJoin> joins) {
        Uri.Builder ub = new Uri.Builder().scheme(uri.getScheme()).authority(uri.getAuthority());
        for (String pathSegment : uri.getPathSegments()) {
            ub.appendPath(pathSegment);
        }

        if (joins != null) {
            for (FSJoin join : joins) {
                ub.appendQueryParameter(joinMap.get(join.type()), joinTextFrom(join, ForSureAndroidInfoFactory.inst().tableName(uri)));
            }
        }

        return ub.build();
    }

    public static String joinStringFrom(Uri uri) {
        StringBuffer sb = new StringBuffer(uri.getLastPathSegment());
        for (FSJoin.Type type : FSJoin.Type.values()) {
            appendJoinStringTo(sb, type, uri.getQueryParameter(joinMap.get(type)));
        }
        return sb.toString();
    }

    private static void appendJoinStringTo(StringBuffer sb, FSJoin.Type type, String joinQuery) {
        if (Strings.isNullOrEmpty(joinQuery)) {
            return;
        }
        sb.append(" ").append(joinMap.get(type)).append(" ").append(joinQuery);
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
}
