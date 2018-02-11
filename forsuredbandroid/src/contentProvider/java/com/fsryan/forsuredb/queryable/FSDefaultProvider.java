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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureAndroidInfoFactory;
import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.api.sqlgeneration.SqlForPreparedStatement;

import java.util.ArrayList;
import java.util.List;

import static com.fsryan.forsuredb.StatementBinder.bindObjects;
import static com.fsryan.forsuredb.queryable.UriAnalyzer.extractJoinsUnsafe;
import static com.fsryan.forsuredb.queryable.UriAnalyzer.extractOrderingsUnsafe;

/**
 * <p>
 *     The default content provider that integrates with ForSure
 * </p>
 * @author Ryan Scott
 */
public class FSDefaultProvider extends ContentProvider {

    private final DBMSIntegrator sqlGenerator;

    public FSDefaultProvider() {
        this(Sql.generator());
    }

    private FSDefaultProvider(DBMSIntegrator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor/" + ForSureAndroidInfoFactory.inst().tableName(uri);
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        long rowId = FSDBHelper.inst().getWritableDatabase().insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (rowId != -1) {
            final Uri insertedItemUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(insertedItemUri, null);
            return insertedItemUri;
        }
        return null;
    }

    /**
     * <p>Actually an upsert, you can pass in the query parameter UPSERT=true on the {@link Uri}
     * in order to run a transaction which first checks for existence of any records matching the
     * selection criteria. If such a record exists, then all matching records are updated. If such
     * a record does not exist, then one is inserted.
     * @param uri the {@link Uri} describing the resource including any query parameters
     * @param values the {@link ContentValues} to update
     * @param selection the parameterized WHERE clause (prameterized using ?s for values)
     * @param selectionArgs the replacements for the parameterized WHERE clause
     * @return the number of rows affected by the query
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return UriAnalyzer.isForUpsert(uri)
                ? performUpsert(uri, values, selection, selectionArgs)
                : updateInternal(uri, values, selection, selectionArgs);
    }

    @Override
    public int delete(@NonNull Uri uri, final String selection, final String[] selectionArgs) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        final UriAnalyzer analyzer = new UriAnalyzer(uri);
        final FSSelection fsSelection = analyzer.getSelection(selection, selectionArgs);
        List<FSOrdering> orderings = analyzer.getOrderingsUnsafe();
        SqlForPreparedStatement ps = sqlGenerator.createDeleteSql(tableName, fsSelection, orderings);

        int rowsAffected = 0;
        SQLiteStatement statement = null;
        try {
            statement = FSDBHelper.inst().getWritableDatabase().compileStatement(ps.getSql());
            statement.bindAllArgsAsStrings(ps.getReplacements());
            rowsAffected = statement.executeUpdateDelete();
            return rowsAffected;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (rowsAffected > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor =  UriAnalyzer.isForJoin(uri)
                ? performJoinQuery(uri, projection, selection, selectionArgs, sortOrder)
                : performQuery(uri, projection, selection, selectionArgs, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);  // <-- allows CursorLoader to auto reload
        return cursor;
    }

    private Cursor performJoinQuery(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        final UriAnalyzer analyzer = new UriAnalyzer(uri);
        final FSSelection fsSelection = analyzer.getSelection(selection, selectionArgs);
        final List<FSOrdering> ordering = analyzer.getOrderingsUnsafe();
        final List<FSJoin> joins = analyzer.getJoinsUnsafe();
        final List<FSProjection> fsProjections = ProjectionHelper.toFSProjections(analyzer.isDistinct(), projection);
        final SqlForPreparedStatement ps = sqlGenerator.createQuerySql(tableName, joins, fsProjections, fsSelection, ordering);
        return FSDBHelper.inst().getReadableDatabase().rawQuery(ps.getSql(), ps.getReplacements());
    }

    private Cursor performQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        final UriAnalyzer analyzer = new UriAnalyzer(uri);
        final FSSelection fsSelection = analyzer.getSelection(selection, selectionArgs);
        final List<FSOrdering> ordering = analyzer.getOrderingsUnsafe();
        final FSProjection fsProjection = ProjectionHelper.toFSProjection(tableName, analyzer.isDistinct(), projection);
        final SqlForPreparedStatement ps = sqlGenerator.createQuerySql(tableName, fsProjection, fsSelection, ordering);
        return FSDBHelper.inst().getReadableDatabase().rawQuery(ps.getSql(), ps.getReplacements());
    }

    private int performUpsert(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = FSDBHelper.inst().getWritableDatabase();
        db.beginTransaction();
        try {
            int rowsAffected;
            if (hasMatchingRecord(uri, selection, selectionArgs)) {
                rowsAffected = updateInternal(uri, values, selection, selectionArgs);
            } else {
                Uri inserted = insert(uri, values);
                rowsAffected = inserted == null ? 0 : 1;
            }
            if (rowsAffected > 0) {
                db.setTransactionSuccessful();
            }
            return rowsAffected;
        } finally {
            db.endTransaction();
        }
    }

    private int updateInternal(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        List<String> columns = new ArrayList<>(values.keySet());
        final UriAnalyzer analyzer = new UriAnalyzer(uri);
        final FSSelection fsSelection = analyzer.getSelection(selection, selectionArgs);
        List<FSOrdering> orderings = analyzer.getOrderingsUnsafe();
        SqlForPreparedStatement ps = sqlGenerator.createUpdateSql(tableName, columns, fsSelection, orderings);

        int rowsAffected = 0;
        SQLiteStatement statement = null;
        try {
            statement = FSDBHelper.inst().getWritableDatabase().compileStatement(ps.getSql());
            bindObjects(statement, columns, values);
            if (ps.getReplacements() != null) {
                for (int pos = 0; pos < ps.getReplacements().length; pos++) {
                    statement.bindString(pos + columns.size() + 1, ps.getReplacements()[pos]);
                }
            }
            rowsAffected = statement.executeUpdateDelete();
            return rowsAffected;
        } catch (SQLException sqle) {
            return 0;   // TODO: propagate?
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (rowsAffected > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
    }

    private boolean hasMatchingRecord(Uri uri, String selection, String[] selectionArgs) {
        Cursor c = query(uri, null, selection, selectionArgs, null);
        try {
            return c != null && c.moveToFirst();
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
