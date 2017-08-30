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
import android.support.annotation.Nullable;

import com.fsryan.forsuredb.ForSureAndroidInfoFactory;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.fsryan.forsuredb.provider.UriEvaluator.isSpecificRecordUri;
import static com.fsryan.forsuredb.provider.UriEvaluator.limitFrom;
import static com.fsryan.forsuredb.provider.UriEvaluator.offsetFrom;
import static com.fsryan.forsuredb.provider.UriEvaluator.offsetFromLast;
import static com.fsryan.forsuredb.provider.UriEvaluator.sortFrom;

/**
 * <p>
 *     Helps ensure properly formatted queries based upon the {@link Uri uri}, selection String,
 *     and selectionArgs passed into the constructor.
 * </p>
 * @author Ryan Scott
 */
/*package*/ class QueryCorrector {

    private static final String ID_SELECTION = "_id = ?";
    private static final Pattern ID_SELECTION_PATTERN = Pattern.compile("_id *(=|IS) *\\?");

    private final String tableName;
    private final String where;
    private final String[] selectionArgs;
    private final String orderBy;
    private final int offset;
    private final int limit;
    private final boolean findingLast;

    public QueryCorrector(@NonNull Uri uri, @Nullable String where, @Nullable String[] whereArgs) {
        this(
                uri,
                where == null ? "" : where,
                whereArgs == null ? new String[0] : whereArgs,
                sortFrom(uri),
                offsetFrom(uri),
                limitFrom(uri),
                offsetFromLast(uri)
        );
    }

    /*package*/ QueryCorrector(@NonNull Uri uri, @NonNull String where, @NonNull String[] selectionArgs, @NonNull String orderBy, int offset, int limit, boolean findingLast) {
        this.tableName = ForSureAndroidInfoFactory.inst().tableName(uri);

        final boolean forSpecificRecord = isSpecificRecordUri(uri);
        this.where = forSpecificRecord ? ensureIdInSelection(where) : where;
        this.selectionArgs = forSpecificRecord ? ensureIdInSelectionArgs(uri, selectionArgs) : selectionArgs;
        this.orderBy = orderBy;
        this.offset = offset;
        this.limit = limit;
        this.findingLast = findingLast;
    }

    public String getSelection(boolean query) {
        // when not finding last or when not
        return query && !findingLast
                ? where
                : "_id IN (SELECT _id FROM " + tableName + " WHERE " + where + " " + orderBy + (limit > 0 ? " LIMIT " + limit : "") + ")";
    }

    public String getOrderBy() {
        return findingLast ? flipOrderBy(orderBy) : orderBy;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    private String ensureIdInSelection(String selection) {
        if (selection == null || selection.isEmpty()) {
            return ID_SELECTION;
        }

        if (!ID_SELECTION_PATTERN.matcher(selection).find()) {
            return ID_SELECTION + " AND (" + selection + ")";    // <-- insert the _id selection if it doesn't exist
        }
        return selection;
    }

    private String[] ensureIdInSelectionArgs(Uri uri, String[] selectionArgs) {
        if (!selectionWasModified(where, selectionArgs)) {
            return selectionArgs;
        }

        final List<String> selectionArgList = new ArrayList<>();
        if (selectionArgs != null) {
            selectionArgList.addAll(Arrays.asList(selectionArgs));
        }
        selectionArgList.add(0, uri.getLastPathSegment());  // <-- prepend because the modified selection string specifies the _id selection first
        return selectionArgList.toArray(new String[selectionArgList.size()]);
    }

    // If the selection was modified, then the number of ? in the selection will be one more than the length of selectionArgs
    private boolean selectionWasModified(String selection, String[] selectionArgs) {
        final int qMarkOccurrences = occurrencesOf('?', selection);
        return (selectionArgs == null && qMarkOccurrences != 0) || qMarkOccurrences == selectionArgs.length + 1;
    }

    /**
     * <p>
     *     an approximation of the one available in the Apache Commons library's StringUtils
     *     countMatches method geared towards counts of characters
     * </p>
     * @param c character to count
     * @param str String in which to count characters
     * @return number of times c appears in str
     */
    private int occurrencesOf(char c, String str) {
        if (str == null) {
            return 0;
        }
        return str.replaceAll("[^" + c + "]", "").length();
    }
}
