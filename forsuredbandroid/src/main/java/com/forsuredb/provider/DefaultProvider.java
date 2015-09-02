/*
   forsuredb, an object relational mapping tool

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
package com.forsuredb.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.forsuredb.FSTableDescriber;
import com.forsuredb.ForSure;

public class DefaultProvider extends ContentProvider {

    public static final String AUTHORITY = "com.forsuredb.default.content";

    public DefaultProvider() {}

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final FSTableDescriber fsTableDescriber = ContentProviderHelper.resolveUri(uri);
        if (fsTableDescriber == null) {
            return null;
        }
        return fsTableDescriber.getMimeType();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final FSTableDescriber fsTableDescriber = ContentProviderHelper.resolveUri(uri);
        if (fsTableDescriber == null) {
            return null;
        }

        final SQLiteDatabase db = ForSure.inst().getWritableDatabase();
        long rowId = db.insertWithOnConflict(fsTableDescriber.getName(), null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (rowId != -1) {
            final Uri insertedItemUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(insertedItemUri, null);
            return insertedItemUri;
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        FSTableDescriber fsTableDescriber = ContentProviderHelper.resolveUri(uri);
        if (fsTableDescriber == null) {
            return 0;
        }

        final boolean singleRecord = ContentProviderHelper.isSingleRecord(uri);
        selection = singleRecord ? ContentProviderHelper.ensureIdInSelection(selection) : selection;
        selectionArgs = singleRecord ? ContentProviderHelper.ensureIdInSelectionArgs(uri, selection, selectionArgs) : selectionArgs;

        final SQLiteDatabase db = ForSure.inst().getWritableDatabase();
        final int rowsAffected = db.update(fsTableDescriber.getName(), values, selection, selectionArgs);
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        FSTableDescriber fsTableDescriber = ContentProviderHelper.resolveUri(uri);
        if (fsTableDescriber == null) {
            return 0;
        }

        final boolean singleRecord = ContentProviderHelper.isSingleRecord(uri);
        selection = singleRecord ? ContentProviderHelper.ensureIdInSelection(selection) : selection;
        selectionArgs = singleRecord ? ContentProviderHelper.ensureIdInSelectionArgs(uri, selection, selectionArgs) : selectionArgs;

        final SQLiteDatabase db = ForSure.inst().getWritableDatabase();
        final int rowsAffected = db.delete(fsTableDescriber.getName(), selection, selectionArgs);
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        FSTableDescriber fsTableDescriber = ContentProviderHelper.resolveUri(uri);
        if (fsTableDescriber == null) {
            return null;
        }

        final boolean singleRecord = ContentProviderHelper.isSingleRecord(uri);
        selection = singleRecord ? ContentProviderHelper.ensureIdInSelection(selection) : selection;
        selectionArgs = singleRecord ? ContentProviderHelper.ensureIdInSelectionArgs(uri, selection, selectionArgs) : selectionArgs;

        final SQLiteDatabase db = ForSure.inst().getReadableDatabase();
        final Cursor cursor = db.query(fsTableDescriber.getName(), projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);  // <-- allows CursorLoader to auto reload
        return cursor;
    }
}