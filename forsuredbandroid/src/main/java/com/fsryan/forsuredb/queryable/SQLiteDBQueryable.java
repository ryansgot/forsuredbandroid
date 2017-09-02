package com.fsryan.forsuredb.queryable;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.cursor.FSCursor;

import java.util.List;

import static com.fsryan.forsuredb.queryable.ProjectionHelper.formatProjection;

public class SQLiteDBQueryable implements FSQueryable<Uri, FSContentValues> {

    // TODO: account for possible usage of first/last and orderings

    /*package*/ interface DBProvider {
        SQLiteDatabase writeableDb();
        SQLiteDatabase readableDb();
    }

    public static final String AUTHORITY = "forsuredb";
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
        return id > 0L ? toUri(id) : null;
    }

    @Override
    public int update(FSContentValues cv, FSSelection selection, List<FSOrdering> orderings) {
        final QueryCorrector qc = new QueryCorrector(tableToQuery, null, selection, Sql.generator().expressOrdering(orderings));
        return dbProvider.writeableDb().update(tableToQuery, cv.getContentValues(), qc.getSelection(false), qc.getSelectionArgs());
    }

    @Override
    public int delete(FSSelection selection, List<FSOrdering> orderings) {
        final QueryCorrector qc = new QueryCorrector(tableToQuery, null, selection, Sql.generator().expressOrdering(orderings));
        return dbProvider.writeableDb().delete(tableToQuery, qc.getSelection(false), qc.getSelectionArgs());
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, List<FSOrdering> orderings) {
        final String[] p = formatProjection(projection);
        final String orderBy = Sql.generator().expressOrdering(orderings);
        final QueryCorrector qc = new QueryCorrector(tableToQuery, null, selection, orderBy);
        final String limit = qc.getLimit() > 0 ? "LIMIT " + qc.getLimit() : null;
        final boolean distinct = projection != null && projection.isDistinct();
        return (FSCursor) dbProvider.readableDb()
                .query(distinct, tableToQuery, p, qc.getSelection(true), qc.getSelectionArgs(), null, null, orderBy, limit);
    }

    @Override
    public Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings) {
        final QueryCorrector qc = new QueryCorrector(tableToQuery, joins, selection, Sql.generator().expressOrdering(orderings));
        final String sql = buildJoinQuery(projections, qc);
        return (FSCursor) dbProvider.readableDb().rawQuery(sql, qc.getSelectionArgs());
    }

    private String buildJoinQuery(List<FSProjection> projections, QueryCorrector qc) {
        final StringBuilder buf = new StringBuilder("SELECT ");

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

        // TODO: using string concatenation in the string buffer is a little smelly
        final String joinString = qc.getJoinString();
        final String where = qc.getSelection(true);
        final String orderBy = qc.getOrderBy();
        return buf.append(" FROM ").append(tableToQuery)
                .append(joinString.isEmpty() ? "" : " " + joinString)       // joins
                .append(where.isEmpty() ? "" : " WHERE " + where)           // selection
                .append(orderBy.isEmpty() ? "" : " ORDER BY " + orderBy)    // ordering
                .append(qc.getLimit() > 0 ? " LIMIT " + qc.getLimit() : "") // limit
                .append(';')
                .toString();
    }

    private Uri toUri(long inserted) {
        return Uri.parse(String.format(URI_FORMAT, tableToQuery, inserted));
    }
}
