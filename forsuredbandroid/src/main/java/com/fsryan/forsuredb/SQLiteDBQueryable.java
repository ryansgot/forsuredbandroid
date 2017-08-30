package com.fsryan.forsuredb;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.cursor.FSCursor;
import com.fsryan.forsuredb.provider.FSContentValues;

import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.ProjectionHelper.formatProjection;

/*package*/ class SQLiteDBQueryable implements FSQueryable<Uri, FSContentValues> {

    // TODO: account for possible usage of first/last and orderings

    /*package*/ interface DBProvider {
        SQLiteDatabase writeableDb();
        SQLiteDatabase readableDb();
    }

    public static String AUTHORITY = "forsuredb";
    private static final String URI_FORMAT = "content://" + AUTHORITY + "/%s/%d";

    private static final DBProvider realProvider = new DBProvider() {
        @NonNull
        @Override
        public SQLiteDatabase writeableDb() {
            return FSDBHelper.inst().getWritableDatabase();
        }

        @NonNull
        @Override
        public SQLiteDatabase readableDb() {
            return FSDBHelper.inst().getReadableDatabase();
        }
    };

    private final String tableToQuery;
    private final DBProvider dbProvider;

    public SQLiteDBQueryable(String tableToQuery) {
        this(tableToQuery, realProvider);
    }

    @VisibleForTesting
    /*package*/ SQLiteDBQueryable(String tableToQuery, DBProvider dbProvider) {
        this.tableToQuery = tableToQuery;
        this.dbProvider = dbProvider;
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
        if (cv.getContentValues().keySet().isEmpty()) {
            cv.put("deleted", 0);
        }

        long id = dbProvider.writeableDb().insert(tableToQuery, null, cv.getContentValues());
        if (id > 0L) {

        }
        return toUri(id);
    }

    @Override
    public int update(FSContentValues cv, FSSelection selection, List<FSOrdering> ordering) {
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return dbProvider.writeableDb().update(tableToQuery, cv.getContentValues(), s, sArgs);
    }

    @Override
    public int delete(FSSelection selection, List<FSOrdering> orderings) {
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return dbProvider.writeableDb().delete(tableToQuery, s, sArgs);
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, List<FSOrdering> orderings) {
        final String[] p = formatProjection(projection);
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        final String sortOrder = Sql.generator().expressOrdering(orderings);
        final String limit = selection == null || selection.limits() == null || selection.limits().count() <= 0 ? null : "LIMIT " + selection.limits();
        return (FSCursor) dbProvider.readableDb()
                .query(projection.isDistinct(), tableToQuery, p, s, sArgs, null, null, sortOrder, limit);
    }

    @Override
    public Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings) {
        final String[] sArgs = selection == null ? null : selection.replacements();
        final String sql = buildJoinQuery(joins, projections, selection, orderings);
        return (FSCursor) dbProvider.readableDb().rawQuery(sql, sArgs);
    }

    // TODO: move this to SQLGenerator
    // TODO: this does not account for inner query when sorting and selecting last
    private String buildJoinQuery(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings) {
        final String where = selection == null ? null : selection.where();
        final StringBuilder buf = new StringBuilder("SELECT ");
        final String sortOrder = Sql.generator().expressOrdering(orderings);

        // projection
        final String[] p = formatProjection(projections);
        if (p == null || p.length == 0) {
            buf.append("* ");
        } else {
            for (String column : p) {
                buf.append(column).append(", ");
            }
            buf.delete(buf.length() - 2, buf.length());
        }

        buf.append(" FROM ").append(tableToQuery);

        // joins
        if (joins != null && !joins.isEmpty()) {
            for (FSJoin join : joins) {
                buf.append(" JOIN ");
                if (tableToQuery.equals(join.getChildTable())) {
                    buf.append(join.getParentTable());
                } else {
                    buf.append(join.getChildTable());
                }
                buf.append(" ON ");
                for (Map.Entry<String, String> colEntry : join.getChildToParentColumnMap().entrySet()) {
                    buf.append(join.getChildTable()).append('.').append(colEntry.getKey())
                            .append('=')
                            .append(join.getParentTable()).append('.').append(colEntry.getValue())
                            .append(" AND ");
                }
                buf.delete(buf.length() - 5, buf.length());
            }
        }

        // where
        if (where != null && !where.isEmpty()) {
            buf.append(" WHERE ").append(where);
        }

        // sort
        if (sortOrder != null) {
            buf.append(sortOrder);
        }

        // limit
        if (selection != null && selection.limits() != null && selection.limits().count() >= 0) {
            buf.append(" LIMIT ").append(selection.limits().count());
        }

        return buf.append(';').toString();
    }

    private Uri toUri(long inserted) {
        return Uri.parse(String.format(URI_FORMAT, tableToQuery, inserted));
    }
}
