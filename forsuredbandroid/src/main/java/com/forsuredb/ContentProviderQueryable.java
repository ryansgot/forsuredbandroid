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
package com.forsuredb;

import android.content.Context;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.forsuredb.api.FSJoin;
import com.forsuredb.api.FSProjection;
import com.forsuredb.api.FSQueryable;
import com.forsuredb.api.FSSelection;
import com.forsuredb.api.Retriever;
import com.forsuredb.provider.QueryCorrector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*package*/ class ContentProviderQueryable implements FSQueryable<Uri, FSContentValues> {

    private final Context appContext;
    private final Uri resource;

    public ContentProviderQueryable(Context appContext, Uri resource) {
        this.appContext = appContext;
        this.resource = resource;
    }

    @Override
    public Uri insert(FSContentValues cv) {
        return appContext.getContentResolver().insert(resource, cv.getContentValues());
    }

    @Override
    public int update(FSContentValues cv, FSSelection selection) {
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return appContext.getContentResolver().update(resource, cv.getContentValues(), s, sArgs);
    }

    @Override
    public int delete(FSSelection selection) {
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return appContext.getContentResolver().delete(resource, s, sArgs);
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, String sortOrder) {
        final String[] p = projection == null ? null : projection.columns();
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return new FSCursor(appContext.getContentResolver().query(resource, p, s, sArgs, sortOrder));
    }

    @Override
    public Retriever query(FSJoin<Uri> join, FSProjection parentProjection, FSProjection childProjection, FSSelection selection, String sortOrder) {
        QueryCorrector qc = new QueryCorrector(join.parentResource(), selection == null ? null : selection.where(), selection == null ? null : selection.replacements());
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(queryBuilderTables(join));
        builder.setProjectionMap(queryBuilderProjectionMap(join, parentProjection, childProjection));
        return new FSCursor(builder.query(FSDBHelper.inst().getReadableDatabase(),
                null,
                qc.getSelection(),
                qc.getSelectionArgs(),
                null,
                null,
                sortOrder));
    }

    private Map<String, String> queryBuilderProjectionMap(FSJoin<Uri> join, FSProjection parentProjection, FSProjection childProjection) {
        Map<String, String> projectionMap = new HashMap<>();
        appendAllToProjectionMap(projectionMap, join.parentTable(), parentProjection);
        appendAllToProjectionMap(projectionMap, join.childTable(), childProjection);
        return projectionMap;
    }

    private void appendAllToProjectionMap(Map<String, String> projectionMap, String tableName, FSProjection projection) {
        if (projection == null || projection.columns() == null) {
            return;
        }
        for (String column : projection.columns()) {
            String unambiguousName = tableName + "." + column;
            projectionMap.put(unambiguousName, unambiguousName + " AS " + tableName + "_" + column);
        }
    }

    private String queryBuilderTables(FSJoin<Uri> join) {
        return String.format("%s %s %s ON (%s.%s %s %s.%s)",
                join.parentTable(),
                join.kind().toString(),
                join.childTable(),
                join.parentTable(),
                join.parentColumn(),
                join.operator().getSymbol(),
                join.childTable(),
                join.childColumn());
    }
}
