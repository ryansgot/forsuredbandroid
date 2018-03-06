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

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Limits;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.api.adapter.SaveResultFactory;
import com.fsryan.forsuredb.cursor.FSCursor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.fsryan.forsuredb.queryable.ProjectionHelper.formatProjection;
import static com.fsryan.forsuredb.queryable.ProjectionHelper.isDistinct;
import static com.fsryan.forsuredb.queryable.ReplacementSerializer.serializeAll;

public class ContentProviderQueryable implements FSQueryable<Uri, FSContentValues> {

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
    public int update(FSContentValues cv, FSSelection selection, List<FSOrdering> orderings) {
        final Uri uri = enrichUri(selection, orderings, false);
        return selection == null
                ? appContext.getContentResolver().update(uri, cv.getContentValues(),null, null)
                : appContext.getContentResolver().update(uri, cv.getContentValues(), selection.where(), serializeAll(selection.replacements()));
    }

    @Override
    public SaveResult<Uri> upsert(FSContentValues cv, FSSelection selection, List<FSOrdering> orderings) {
        final Uri uri = enrichUri(selection, orderings, true);
        try {
            int rowsAffected = selection == null
                    ? appContext.getContentResolver().update(uri, cv.getContentValues(), null, null)
                    : appContext.getContentResolver().update(uri, cv.getContentValues(), selection.where(), serializeAll(selection.replacements()));
            return SaveResultFactory.create(null, rowsAffected, null);
        } catch (Exception e) {
            return SaveResultFactory.create(null, 0, e);
        }
    }

    @Override
    public int delete(FSSelection selection, List<FSOrdering> orderings) {
        final Uri uri = enrichUri(selection, orderings, false);
        return selection == null
                ? appContext.getContentResolver().delete(uri, null, null)
                : appContext.getContentResolver().delete(uri, selection.where(), serializeAll(selection.replacements()));
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, List<FSOrdering> orderings) {
        final String[] p = formatProjection(Arrays.asList(projection));
        final Uri uri = enrichUri(projection, selection, orderings, false);
        return selection == null
                ? new FSCursor(appContext.getContentResolver().query(uri, p, null, null, null))
                : new FSCursor(appContext.getContentResolver().query(uri, p, selection.where(), serializeAll(selection.replacements()), null));
    }

    @Override
    public Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings) {
        final String[] p = formatProjection(projections);
        final Uri uri = enrichUri(projections, selection, orderings, joins, false);
        return selection == null
                ? new FSCursor(appContext.getContentResolver().query(uri, p, null, null, null))
                : new FSCursor(appContext.getContentResolver().query(uri, p, selection.where(), serializeAll(selection.replacements()), null));
    }

    private Uri enrichUri(@Nullable FSSelection selection, @Nullable List<FSOrdering> orderings, boolean upsert) {
        return enrichUri((FSProjection) null, selection, orderings, upsert);
    }

    private Uri enrichUri(@Nullable FSProjection projection,
                          @Nullable FSSelection selection,
                          @Nullable List<FSOrdering> orderings,
                          boolean upsert) {
        return enrichUri(
                projection == null ? Collections.<FSProjection>emptyList() : Arrays.asList(projection),
                selection,
                orderings,
                null,
                upsert
        );
    }

    private Uri enrichUri(@NonNull List<FSProjection> projections,
                          @Nullable FSSelection selection,
                          @Nullable List<FSOrdering> orderings,
                          @Nullable List<FSJoin> joins,
                          boolean upsert) {
        return enrichUri(
                projections,
                selection == null || selection.limits() == null ? Limits.NONE : selection.limits(),
                orderings == null ? Collections.<FSOrdering>emptyList() : orderings,
                joins == null ? Collections.<FSJoin>emptyList() : joins,
                upsert
        );
    }

    private Uri enrichUri(@NonNull List<FSProjection> projections,
                          @NonNull Limits limits,
                          @NonNull List<FSOrdering> orderings,
                          @NonNull List<FSJoin> joins,
                          boolean upsert) {
        Uri.Builder builder = resource.buildUpon();
        // TODO: this could fail upon passing in a URI that already has different limits. Account for this situation
        if (limits.count() != 0 || limits.offset() > 0) {
            builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_LIMITS, UriAnalyzer.stringify(limits));
        }
        if (isDistinct(projections) && !UriAnalyzer.isForDistinctQuery(resource)) {
            builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_DISTINCT, String.valueOf(true));
        }
        if (upsert && !UriAnalyzer.isForUpsert(resource)) {
            builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_UPSERT, String.valueOf(true));
        }

        List<FSOrdering> includedOrderings = UriAnalyzer.extractOrderingsFrom(resource);
        for (FSOrdering ordering : orderings) {
            boolean included = false;
            for (FSOrdering includedOrdering : includedOrderings) {
                if (EQHelper.orderingEquals(ordering, includedOrdering)) {
                    included = true;
                    break;
                }
            }
            if (!included) {
                builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_ORDERING, UriAnalyzer.stringify(ordering));
            }
        }

        List<FSJoin> includedJoins = UriAnalyzer.extractJoinsFrom(resource);
        for (FSJoin join : joins) {
            boolean included = false;
            for (FSJoin includedJoin : includedJoins) {
                if (EQHelper.joinEquals(join, includedJoin)) {
                    included = true;
                    break;
                }
            }
            if (!included) {
                builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_JOIN, UriAnalyzer.stringify(join));
            }
        }
        return builder.build();
    }
}
