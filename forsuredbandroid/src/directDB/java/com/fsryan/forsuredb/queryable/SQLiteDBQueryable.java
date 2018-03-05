package com.fsryan.forsuredb.queryable;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
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
import com.fsryan.forsuredb.api.sqlgeneration.SqlForPreparedStatement;

import java.util.ArrayList;
import java.util.List;

import static com.fsryan.forsuredb.SqlBinder.bindObjects;

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

        List<String> columns = new ArrayList<>(cv.keySet());
        String sql = sqlGenerator.newSingleRowInsertionSql(locator.table, columns);

        SQLiteStatement statement = null;
        try {
            statement = FSDBHelper.inst().getWritableDatabase().compileStatement(sql);
            bindObjects(statement, columns, cv);
            long id = statement.executeInsert();
            return id < 1 ? null : new DirectLocator(locator.table, id);
        } catch (SQLException sqle) {
            return null;    // TODO: propagate instead of trap
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    @Override
    public int update(FSContentValues cv, FSSelection selection, List<FSOrdering> orderings) {
        List<String> columns = new ArrayList<>(cv.keySet());
        SqlForPreparedStatement ps = sqlGenerator.createUpdateSql(locator.table, columns, selection, orderings);
        SQLiteStatement statement = null;
        try {
            statement = FSDBHelper.inst().getWritableDatabase().compileStatement(ps.getSql());
            bindObjects(statement, columns, cv);
            bindObjects(statement, columns.size() + 1, ps.getReplacements());
            return statement.executeUpdateDelete();
        } catch (SQLException sqle) {
            return 0;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    @Override
    public SaveResult<DirectLocator> upsert(FSContentValues cv, FSSelection selection, List<FSOrdering> orderings) {
        SQLiteDatabase db = FSDBHelper.inst().getWritableDatabase();
        db.beginTransaction();
        try {
            DirectLocator inserted = null;
            int rowsAffected;
            if (hasMatchingRecord(selection)) {
                rowsAffected = update(cv, selection, orderings);
            } else {
                inserted = insert(cv);
                rowsAffected = inserted == null ? 0 : 1;
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
        SqlForPreparedStatement ps = sqlGenerator.createDeleteSql(locator.table, selection, orderings);
        SQLiteStatement statement = null;
        try {
            statement = FSDBHelper.inst().getWritableDatabase().compileStatement(ps.getSql());
            bindObjects(statement, ps.getReplacements());
            return statement.executeUpdateDelete();
        } catch (SQLException sqle) {
            return 0;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, List<FSOrdering> orderings) {
        SqlForPreparedStatement ps = sqlGenerator.createQuerySql(
                locator.table,
                projection,
                selection,
                orderings
        );

        final SQLiteDatabase db = FSDBHelper.inst().getReadableDatabase();
        return new CursorDriverHack(db, locator.table).query(ps);
//        final String[] bindStrs = ReplacementStringifier.stringifyAll(ps.getReplacements());
//        return (FSCursor) FSDBHelper.inst().getReadableDatabase().rawQuery(ps.getSql(), bindStrs);
    }

    @Override
    public Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, List<FSOrdering> orderings) {
        SqlForPreparedStatement ps = sqlGenerator.createQuerySql(
                locator.table,
                joins,
                projections,
                selection,
                orderings
        );
        final SQLiteDatabase db = FSDBHelper.inst().getReadableDatabase();
        return new CursorDriverHack(db, locator.table).query(ps);
//        final String[] bindStrs = ReplacementStringifier.stringifyAll(ps.getReplacements());
//        return (FSCursor) FSDBHelper.inst().getReadableDatabase().rawQuery(ps.getSql(), bindStrs);
    }

    private boolean hasMatchingRecord(FSSelection selection) {
        Retriever r = null;
        try {
            r = query(null, selection, null);
            return r != null && r.moveToFirst();
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }
}
