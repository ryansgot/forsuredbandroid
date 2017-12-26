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
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.cursor.FSCursor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.fsryan.forsuredb.queryable.ProjectionHelper.formatProjection;
import static com.fsryan.forsuredb.queryable.ProjectionHelper.isDistinct;
import static com.fsryan.forsuredb.queryable.UriEvaluator.FIRST_QUERY_PARAM;
import static com.fsryan.forsuredb.queryable.UriEvaluator.OFFSET_QUERY_PARAM;
import static com.fsryan.forsuredb.queryable.UriEvaluator.LAST_QUERY_PARAM;
import static com.fsryan.forsuredb.queryable.UriEvaluator.ORDER_BY_QUERY_PARM;

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
        final Uri uri = enrichUri(selection, orderings);
        return selection == null
                ? appContext.getContentResolver().update(uri, cv.getContentValues(),null, null)
                : appContext.getContentResolver().update(uri, cv.getContentValues(), selection.where(), selection.replacements());
    }

    @Override
    public int delete(FSSelection selection, List<FSOrdering> orderings) {
        final Uri uri = enrichUri(selection, orderings);
        return selection == null
                ? appContext.getContentResolver().delete(uri, null, null)
                : appContext.getContentResolver().delete(uri, selection.where(), selection.replacements());
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, List<FSOrdering> orderings) {
        final String[] p = formatProjection(Arrays.asList(projection));
        final Uri uri = enrichUri(projection, selection, orderings);
        final String orderBy = Sql.generator().expressOrdering(orderings).replace("ORDER BY", "").trim();
        return selection == null
                ? new FSCursor(appContext.getContentResolver().query(uri, p, null, null, orderBy))
                : new FSCursor(appContext.getContentResolver().query(uri, p, selection.where(), selection.replacements(), orderBy));
    }

    @Override
    public Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings) {
        final String[] p = formatProjection(projections);
        final Uri uri = enrichUri(projections, selection, orderings);
        final String orderBy = Sql.generator().expressOrdering(orderings).replace("ORDER BY", "").trim();
        return selection == null
                ? new FSCursor(appContext.getContentResolver().query(uri, p, null, null, orderBy))
                : new FSCursor(appContext.getContentResolver().query(uri, p, selection.where(), selection.replacements(), orderBy));
    }

    // TODO: create separate class for enriching Uris
    private Uri enrichUri(@Nullable FSSelection selection, @Nullable List<FSOrdering> orderings) {
        return enrichUri((FSProjection) null, selection, orderings);
    }

    private Uri enrichUri(@Nullable FSProjection projection, @Nullable FSSelection selection, @Nullable List<FSOrdering> orderings) {
        return enrichUri(
                projection == null ? Collections.<FSProjection>emptyList() : Arrays.asList(projection),
                selection,
                orderings
        );
    }

    private Uri enrichUri(@NonNull List<FSProjection> projections, @Nullable FSSelection selection, @Nullable List<FSOrdering> orderings) {
        return enrichUri(
                projections,
                selection == null || selection.limits() == null ? Limits.NONE : selection.limits(),
                orderings == null ? Collections.<FSOrdering>emptyList() : orderings
        );
    }

    private Uri enrichUri(@NonNull List<FSProjection> projections, @NonNull Limits limits, @NonNull List<FSOrdering> orderings) {
        Uri.Builder builder = resource.buildUpon()
                .appendQueryParameter("DISTINCT", isDistinct(projections) ? "true" : "false");

        if (limits.offset() > 0 || limits.count() > 0) {
            final String limitType = limits.isBottom() ? LAST_QUERY_PARAM : FIRST_QUERY_PARAM;
            builder.appendQueryParameter(limitType, String.valueOf(limits.count()))
                    .appendQueryParameter(OFFSET_QUERY_PARAM, String.valueOf(limits.offset()));
        }

        if (!orderings.isEmpty()) {
            builder.appendQueryParameter(ORDER_BY_QUERY_PARM, Sql.generator().expressOrdering(orderings));
        }
        return builder.build();
    }
}
