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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureAndroidInfoFactory;

import static com.fsryan.forsuredb.queryable.UriEvaluator.isJoin;

/**
 * <p>
 *     The default content provider that integrates with ForSure
 * </p>
 * @author Ryan Scott
 */
public class FSDefaultProvider extends ContentProvider {

    public FSDefaultProvider() {}

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

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        final QueryCorrector qc = new UriQueryCorrector(uri, selection, selectionArgs);
        final int rowsAffected = FSDBHelper.inst().getWritableDatabase().update(tableName, values, qc.getSelection(false), qc.getSelectionArgs());
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        final QueryCorrector qc = new UriQueryCorrector(uri, selection, selectionArgs);

        if (UriEvaluator.hasFirstOrLastParam(uri)) {
            FSDBHelper.inst().getWritableDatabase().delete(tableName, qc.getSelection(false), qc.getSelectionArgs());
        }

        final int rowsAffected = FSDBHelper.inst().getWritableDatabase().delete(tableName, qc.getSelection(false), qc.getSelectionArgs());
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = isJoin(uri)
                ? performJoinQuery(uri, projection, selection, selectionArgs, sortOrder)
                : performQuery(uri, projection, selection, selectionArgs, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);  // <-- allows CursorLoader to auto reload
        return cursor;
    }

    private Cursor performJoinQuery(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        QueryCorrector qc = new UriQueryCorrector(uri, selection, selectionArgs);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(UriJoinTranslator.joinStringFrom(uri));
        boolean isDistinct = Boolean.parseBoolean(uri.getQueryParameter("DISTINCT"));
        builder.setDistinct(isDistinct);
        final String limit = qc.getLimit() > 0 ? String.valueOf(qc.getLimit()) : null;
        return builder.query(FSDBHelper.inst().getReadableDatabase(),
                projection,
                qc.getSelection(true),
                qc.getSelectionArgs(),
                null,
                null,
                sortOrder,
                limit);
    }

    private Cursor performQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        final QueryCorrector qc = new UriQueryCorrector(uri, selection, selectionArgs);
        final String limit = qc.getLimit() > 0 ? String.valueOf(qc.getLimit()) : null;
        boolean isDistinct = Boolean.parseBoolean(uri.getQueryParameter("DISTINCT"));
        return FSDBHelper.inst().getReadableDatabase().query(isDistinct, tableName, projection, qc.getSelection(true), qc.getSelectionArgs(), null, null, sortOrder, limit);
    }
}
