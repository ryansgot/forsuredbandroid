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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSSelection;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * <p>
 *     Helps ensure properly formatted queries based upon the {@link Uri uri}, selection String,
 *     and selectionArgs passed into the constructor.
 * </p>
 * @author Ryan Scott
 */
/*package*/ class QueryCorrector {

    static final int LIMIT_OFFSET_NO_LIMIT = -1;

    private final String tableName;
    private final String joinString;
    private final String where;
    private final String[] selectionArgs;
    private final String orderBy;
    private final int offset;
    private final int limit;
    private final boolean findingLast;

    public QueryCorrector(@NonNull String tableName, @Nullable List<FSJoin> joins, @Nullable FSSelection selection, @Nullable String orderBy) {
        this(
                tableName,
                joinStringFrom(tableName, joins),
                selection == null || selection.where() == null ? "" : selection.where(),
                selection == null || selection.replacements() == null ? new String[0] : selection.replacements(),
                orderBy == null ? "" : orderBy,
                selection == null || selection.limits() == null ? 0 : selection.limits().offset(),
                selection == null || selection.limits() == null ? 0 : selection.limits().count(),
                selection != null && selection.limits() != null && selection.limits().isBottom()
        );
    }

    /*package*/ QueryCorrector(@NonNull String tableName, @NonNull String joinString, @NonNull String where, @NonNull String[] whereArgs, @NonNull String orderBy, int offset, int limit, boolean findingLast) {
        this.tableName = tableName;
        this.joinString = joinString.isEmpty() ? "" : joinString.substring(tableName.length()).trim(); // <-- due to a quirk with how UriEvaluator works, the beginning will be the base table name;
        this.where = where;
        this.selectionArgs = whereArgs;
        this.orderBy = orderBy.startsWith(" ORDER BY ") ? orderBy.substring(10) : orderBy;  // <-- quirk with the sqlitelib prefixing orderBy with this
        this.offset = offset;
        this.limit = limit;
        this.findingLast = findingLast;
    }

    @NonNull
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

    @NonNull
    public String getOrderBy() {
        return findingLast && orderBy.isEmpty() ? tableName + "._id ASC" : orderBy;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return offset > 0 && limit == 0 ? LIMIT_OFFSET_NO_LIMIT : limit;
    }

    public boolean isFindingLast() {
        return findingLast;
    }

    @NonNull
    public String getJoinString() {
        return joinString;
    }

    @NonNull
    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    private String innerSelectWhereClause() {
        // using rowid because _id is not always populated--will be null for anything that uses a non-default PRIMARY KEY
        return tableName + ".rowid IN (SELECT " + tableName + ".rowid FROM "
                + tableName
                + (joinString.isEmpty() ? "" : " " + joinString)
                + (where.isEmpty() ? "" : " WHERE " + where)
                + " ORDER BY "
                + (orderBy.isEmpty()
                        ?  tableName + "._id " + (findingLast ? "DESC" : "ASC")
                        : (findingLast ? flipOrderBy() : orderBy).trim())
                + (getLimit() != 0 ? " LIMIT " + getLimit() : "")
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

    private static String joinStringFrom(@NonNull String baseTableName, @Nullable List<FSJoin> joins) {
        if (joins == null || joins.isEmpty()) {
            return "";
        }

        Set<String> joinedTables = new HashSet<>(2);
        joinedTables.add(baseTableName);

        StringBuilder buf = new StringBuilder(baseTableName);
        for (FSJoin join : joins) {
            final String tableToJoin = joinedTables.contains(join.getChildTable()) ? join.getParentTable() : join.getChildTable();
            joinedTables.add(tableToJoin);

            buf.append(" JOIN ").append(tableToJoin).append(" ON ");
            for (Map.Entry<String, String> colEntry : join.getChildToParentColumnMap().entrySet()) {
                buf.append(join.getChildTable()).append('.').append(colEntry.getKey())
                        .append('=')
                        .append(join.getParentTable()).append('.').append(colEntry.getValue())
                        .append(" AND ");
            }
            buf.delete(buf.length() - 5, buf.length());
        }

        return buf.toString();
    }

}
