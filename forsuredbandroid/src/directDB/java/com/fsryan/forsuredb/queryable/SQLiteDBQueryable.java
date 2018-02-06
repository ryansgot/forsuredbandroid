package com.fsryan.forsuredb.queryable;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.api.adapter.SaveResultFactory;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.cursor.FSCursor;

import java.util.List;

import static com.fsryan.forsuredb.queryable.ProjectionHelper.formatProjection;

public class SQLiteDBQueryable implements FSQueryable<DirectLocator, FSContentValues> {

    private final DBMSIntegrator sqlGenerator;
    private final DirectLocator locator;

    public SQLiteDBQueryable(@NonNull String tableToQuery) {
        this(new DirectLocator(tableToQuery));
    }

    public SQLiteDBQueryable(@NonNull DirectLocator locator) {
        this(Sql.generator(), locator);
    }

    @VisibleForTesting
    SQLiteDBQueryable(@NonNull DBMSIntegrator sqlGenerator, @NonNull DirectLocator locator) {
        this.sqlGenerator = sqlGenerator;
        this.locator = locator;
    }

    @Override
    public DirectLocator insert(FSContentValues cv) {
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

        long id = FSDBHelper.inst().getWritableDatabase().insert(locator.table, null, cv.getContentValues());
        // TODO: check whether returning null is okay
        return id < 1 ? null : new DirectLocator(locator.table, id);
    }

    @Override
    public int update(FSContentValues cv, FSSelection selection, List<FSOrdering> orderings) {
        final QueryCorrector qc = new QueryCorrector(locator.table, null, selection, sqlGenerator.expressOrdering(orderings));
        return FSDBHelper.inst().getWritableDatabase()
                .update(locator.table, cv.getContentValues(), qc.getSelection(false), qc.getSelectionArgs());
    }

    @Override
    public SaveResult<DirectLocator> upsert(FSContentValues cv, FSSelection selection, List<FSOrdering> orderings) {
        SQLiteDatabase db = FSDBHelper.inst().getWritableDatabase();
        db.beginTransaction();
        try {
            DirectLocator inserted = null;
            int rowsAffected;
            if (countOf(selection) < 1) {
                inserted = insert(cv);
                rowsAffected = 1;
            } else {
                rowsAffected = update(cv, selection, orderings);
            }
            db.setTransactionSuccessful();
            return SaveResultFactory.create(inserted, rowsAffected, null);
        } catch (Exception e) {
            return SaveResultFactory.create(null, 0, e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public int delete(FSSelection selection, List<FSOrdering> orderings) {
        final QueryCorrector qc = new QueryCorrector(locator.table, null, selection, sqlGenerator.expressOrdering(orderings));
        return FSDBHelper.inst().getWritableDatabase()
                .delete(locator.table, qc.getSelection(false), qc.getSelectionArgs());
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, List<FSOrdering> orderings) {
        final boolean distinct = projection != null && projection.isDistinct();
        final String[] p = formatProjection(projection);
        final String orderBy = sqlGenerator.expressOrdering(orderings);
        final QueryCorrector qc = new QueryCorrector(locator.table, null, selection, orderBy);

        // TODO: use the DBMSIntegrator method like you should
        // The following is a terrible hack to make offset without limit work without correctly
        // in an expedient way without correctly using the underlying DBMSIntegrator method
        final String limit = qc.getLimit() == 0 ? null
                : qc.getOffset() == 0 ? String.valueOf(qc.getLimit())
                : qc.getOffset() + "," + Math.abs(qc.getLimit());
        String sql = SQLiteQueryBuilder.buildQueryString(
                distinct,
                locator.table,
                p,
                qc.getSelection(true),
                null,
                null,
                qc.getOrderBy(),
                qc.isFindingLast() ? null : limit
        );
        if (!qc.isFindingLast() && qc.getLimit() == QueryCorrector.LIMIT_OFFSET_NO_LIMIT) {
            sql = sql.replace("LIMIT 1," + qc.getOffset(), "LIMIT -1 OFFSET " + qc.getOffset());
        }
        return (FSCursor) FSDBHelper.inst().getReadableDatabase().rawQuery(sql, qc.getSelectionArgs());
    }

    @Override
    public Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings) {
        final QueryCorrector qc = new QueryCorrector(locator.table, joins, selection, sqlGenerator.expressOrdering(orderings));
        final String sql = buildJoinQuery(projections, qc);
        return (FSCursor) FSDBHelper.inst().getReadableDatabase().rawQuery(sql, qc.getSelectionArgs());
    }

    // TODO: get rid of this ugly code and just use the DBMSIntegrator to handle query generation
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

        final String joinString = qc.getJoinString();
        final String where = qc.getSelection(true);
        final String orderBy = qc.getOrderBy();
        buf.append(" FROM ").append(locator.table)
                .append(joinString.isEmpty() ? "" : " " + joinString)       // joins
                .append(where.isEmpty() ? "" : " WHERE " + where)           // selection
                .append(orderBy.isEmpty() ? "" : " ORDER BY " + orderBy);   // ordering
        if (qc.isFindingLast()) {
            return buf.append(';').toString();
        }
        return buf.append(qc.getLimit() == 0 ? "" : " LIMIT " + qc.getLimit()) // limit
                .append(qc.getOffset() < 1 ? "" : " OFFSET " + qc.getOffset())
                .append(';').toString();
    }

    private int countOf(FSSelection selection) {
        QueryCorrector qc = new QueryCorrector(locator.table, null, selection, null);
        Cursor c = null;
        try {
            SQLiteQueryBuilder sql = new SQLiteQueryBuilder();
            sql.setTables(locator.table);
            c = sql.query(FSDBHelper.inst().getReadableDatabase(),
                    new String[] {"COUNT(*)"},
                    qc.getSelection(true),
                    qc.getSelectionArgs(),
                    null,
                    null,
                    null
            );

            return c == null || !c.moveToFirst() ? 0 : c.getInt(0);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
