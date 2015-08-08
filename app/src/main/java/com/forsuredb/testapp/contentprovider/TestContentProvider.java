package com.forsuredb.testapp.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.forsuredb.ForSure;
import com.forsuredb.FSTableDescriber;

public class TestContentProvider extends ContentProvider {

    public static final String AUTHORITY ="com.forsuredb.testapp.content";

    public TestContentProvider() {}

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
