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
package com.fsryan.forsuredb;

import android.content.Context;
import android.net.Uri;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.cursor.FSCursor;
import com.fsryan.forsuredb.provider.FSContentValues;

import java.util.ArrayList;
import java.util.List;

import static com.fsryan.forsuredb.ProjectionHelper.formatProjection;
import static com.fsryan.forsuredb.ProjectionHelper.isDistinct;

/*package*/ class ContentProviderQueryable implements FSQueryable<Uri, FSContentValues> {

    private final Context appContext;
    private final Uri resource;

    public ContentProviderQueryable(Context appContext, Uri resource) {
        this.appContext = appContext;
        this.resource = resource;
    }

    @Override
    public Uri insert(FSContentValues cv) {
        // SQLite either requires that there be a value for a column in an insert query or that the query be in the following
        // form: INSERT INTO table DEFAULT VALUES;
        // Since executing raw SQL on the SQLiteDatabase reference would achieve the desired result, but return void, we would
        // not get the Uri of the inserted resource back from the call.
        // This hack makes use of the fact that each forsuredb table has a 'deleted' column with a default value of 0. Since it
        // would have been 0 anyway, we can get away with this hack here and can avoid using the nullColumnHack encouraged by
        // the Android framework.
        if (cv.getContentValues().keySet().size() == 0) {
            cv.put("deleted", 0);
        }
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
        final String[] p = formatProjection(projection);
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return new FSCursor(appContext.getContentResolver().query(resourceFrom(resource, projection), p, s, sArgs, sortOrder));
    }

    @Override
    public Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, String sortOrder) {
        final String[] p = formatProjection(projections);
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return new FSCursor(appContext.getContentResolver().query(resourceFrom(resource, projections), p, s, sArgs, sortOrder));
    }

    private static Uri resourceFrom(Uri uri, FSProjection projection) {
        if (projection == null) {
            return uri.buildUpon()
                    .appendQueryParameter("DISTINCT", "false")
                    .build();
        }

        List<FSProjection> projections = new ArrayList<>();
        projections.add(projection);
        return resourceFrom(uri, projections);
    }

    private static Uri resourceFrom(Uri uri, List<FSProjection> projections) {
        return uri.buildUpon()
                .appendQueryParameter("DISTINCT", isDistinct(projections) ? "true" : "false")
                .build();
    }
}
