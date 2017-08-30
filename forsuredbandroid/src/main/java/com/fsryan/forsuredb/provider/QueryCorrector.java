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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.fsryan.forsuredb.provider.UriEvaluator.isSpecificRecordUri;
import static com.fsryan.forsuredb.provider.UriEvaluator.limitFrom;
import static com.fsryan.forsuredb.provider.UriEvaluator.offsetFrom;
import static com.fsryan.forsuredb.provider.UriEvaluator.offsetFromLast;
import static com.fsryan.forsuredb.provider.UriEvaluator.orderingFrom;

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
                orderingFrom(uri),
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

    public String getSelection(boolean retrieval) {
        // if finding last, then there is no choice but run an inner select query
        if (findingLast) {
            return innerSelectWhereClause();
        }
        // If deleting/updating, ContentResolver does not provide the interface necessary to
        // perform limiting/offseting the records that we would like to be affected, so selection
        // must become an inner select query when not retrieving and limit and/or offset set
        return retrieval || (limit <= 0 && offset <= 0) ? where : innerSelectWhereClause();
    }

    public String getOrderBy() {
        return findingLast && orderBy.isEmpty() ? tableName + "._id ASC" : orderBy;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    private String innerSelectWhereClause() {
        return tableName + "._id IN (SELECT " + tableName + "._id FROM "
                + tableName
                + (where.isEmpty() ? "" : " WHERE " + where)
                + " ORDER BY " + (orderBy.isEmpty()
                ? tableName + "._id " + (findingLast ? "DESC" : "ASC")
                : (findingLast ? flipOrderBy() : orderBy).trim())
                + (limit > 0 ? " LIMIT " + limit : "")
                + (offset > 0 ? " OFFSET " + offset : "")
                + ")";
    }

    private String flipOrderBy() {
        if (orderBy == null || orderBy.isEmpty()) {
            return "";
        }

        final String[] split = orderBy.split(" +");
        final StringBuilder buf = new StringBuilder(split[0].isEmpty() ? " " : split[0]);
        for (int i = 1; i < split.length; i++) {
            buf.append(" ");
            switch (split[i]) {
                case "DESC":
                    buf.append("ASC");
                    break;
                case "DESC,":
                    buf.append("ASC,");
                    break;
                case "ASC":
                    buf.append("DESC");
                    break;
                case "ASC,":
                    buf.append("DESC,");
                    break;
                default:
                    buf.append(split[i]);
            }
        }
        return buf.toString();
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
