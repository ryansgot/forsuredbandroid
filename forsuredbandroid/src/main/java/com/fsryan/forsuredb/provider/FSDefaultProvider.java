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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureAndroidInfoFactory;

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
    public Uri insert(Uri uri, ContentValues values) {
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
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        final QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        final int rowsAffected = FSDBHelper.inst().getWritableDatabase().update(tableName, values, qc.getSelection(), qc.getSelectionArgs());
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        final QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        final int rowsAffected = FSDBHelper.inst().getWritableDatabase().delete(tableName, qc.getSelection(), qc.getSelectionArgs());
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = UriEvaluator.isJoin(uri) ? performJoinQuery(uri, projection, selection, selectionArgs, sortOrder)
                                                 : performQuery(uri, projection, selection, selectionArgs, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);  // <-- allows CursorLoader to auto reload
        return cursor;
    }

    private Cursor performJoinQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(UriJoiner.joinStringFrom(uri));
        return builder.query(FSDBHelper.inst().getReadableDatabase(),
                projection,
                qc.getSelection(),
                qc.getSelectionArgs(),
                null,
                null,
                sortOrder);
    }

    private Cursor performQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final String tableName = ForSureAndroidInfoFactory.inst().tableName(uri);
        final QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        return FSDBHelper.inst().getReadableDatabase().query(tableName, projection, qc.getSelection(), qc.getSelectionArgs(), null, null, sortOrder);
    }
}
