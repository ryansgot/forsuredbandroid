package com.fsryan.forsuredb.queryable;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;

import com.fsryan.forsuredb.SqlBinder;
import com.fsryan.forsuredb.api.sqlgeneration.SqlForPreparedStatement;
import com.fsryan.forsuredb.cursor.FSCursor;
import com.fsryan.forsuredb.cursor.FSCursorFactory;

import java.lang.reflect.Constructor;

class CursorDriverHack implements SQLiteCursorDriver {

    private static final Constructor<SQLiteQuery> queryFactory = initQueryFactory();
    private static final FSCursorFactory cursorFactory = new FSCursorFactory();

    private final SQLiteDatabase db;
    private final String editTable;

    public static boolean isAvailable() {
        return queryFactory != null;
    }

    CursorDriverHack(@NonNull SQLiteDatabase db, @NonNull String editTable) {
        this.db = db;
        this.editTable= editTable;
    }

    FSCursor query(@NonNull SqlForPreparedStatement ps) {
        SQLiteQuery query = null;
        db.acquireReference();
        try {
            query = queryFactory.newInstance(db, ps.getSql(), null);
            SqlBinder.bindObjects(query, ps.getReplacements());
            return cursorFactory.newCursor(db, this, editTable, query);
        } catch (Exception e) {
            if (query != null) {
                query.close();
            }
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
        } finally {
            db.releaseReference();
        }
    }

    @Override
    public Cursor query(SQLiteDatabase.CursorFactory factory, String[] bindArgs) {
        return null;
    }

    @Override
    public void cursorDeactivated() {

    }

    @Override
    public void cursorRequeried(Cursor cursor) {

    }

    @Override
    public void cursorClosed() {

    }

    @Override
    public void setBindArguments(String[] bindArgs) {

    }

    private static Constructor<SQLiteQuery> initQueryFactory() {
        try {
            Constructor<SQLiteQuery> ret = SQLiteQuery.class.getDeclaredConstructor(
                    SQLiteDatabase.class,
                    String.class,
                    CancellationSignal.class
            );
            ret.setAccessible(true);
            return ret;
        } catch (Exception e) {
            return null;
        }
    }
}