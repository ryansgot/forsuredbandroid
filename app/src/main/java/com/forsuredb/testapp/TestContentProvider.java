package com.forsuredb.testapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.forsuredb.table.FSTableDescriber;

public class TestContentProvider extends ContentProvider {

    public static final String AUTHORITY ="com.forsuredb.testapp.content";

    private TestDBHelper dbHelper;

    public TestContentProvider() {}

    @Override
    public boolean onCreate() {
        dbHelper = TestDBHelper.getInstance(getContext());
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
        long rowId = dbHelper.getWritableDatabase().insertWithOnConflict(fsTableDescriber.getName(),
                                                                         null,
                                                                         values,
                                                                         SQLiteDatabase.CONFLICT_IGNORE);
        if (rowId != -1) {
            final Uri insertedItemUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(insertedItemUri, null);
            return insertedItemUri;
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Correct selection and selectionArgs if uri specifies a resource ID, but the selection string and selectionArgs do not
        // specify the resource ID
        FSTableDescriber fsTableDescriber = ContentProviderHelper.resolveUri(uri);
        final boolean singleRecord = ContentProviderHelper.isSingleRecord(uri);
        selection = singleRecord ? ContentProviderHelper.ensureIdInSelection(selection) : selection;
        selectionArgs = singleRecord ? ContentProviderHelper.ensureIdInSelectionArgs(uri, selection, selectionArgs) : selectionArgs;

        final int rowsAffected = dbHelper.getWritableDatabase().update(fsTableDescriber.getName(),
                                                                       values,
                                                                       selection,
                                                                       selectionArgs);
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Correct selection and selectionArgs if uri specifies a resource ID, but the selection string and selectionArgs do not
        // specify the resource ID
        FSTableDescriber fsTableDescriber = ContentProviderHelper.resolveUri(uri);
        final boolean singleRecord = ContentProviderHelper.isSingleRecord(uri);
        selection = singleRecord ? ContentProviderHelper.ensureIdInSelection(selection) : selection;
        selectionArgs = singleRecord ? ContentProviderHelper.ensureIdInSelectionArgs(uri, selection, selectionArgs) : selectionArgs;

        final int rowsAffected = dbHelper.getWritableDatabase().delete(fsTableDescriber.getName(), selection, selectionArgs);
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Correct selection and selectionArgs if uri specifies a resource ID, but the selection string and selectionArgs do not
        // specify the resource ID
        FSTableDescriber fsTableDescriber = ContentProviderHelper.resolveUri(uri);
        final boolean singleRecord = ContentProviderHelper.isSingleRecord(uri);
        selection = singleRecord ? ContentProviderHelper.ensureIdInSelection(selection) : selection;
        selectionArgs = singleRecord ? ContentProviderHelper.ensureIdInSelectionArgs(uri, selection, selectionArgs) : selectionArgs;

        final Cursor cursor = dbHelper.getReadableDatabase().query(fsTableDescriber.getName(),
                                                                   projection,
                                                                   selection,
                                                                   selectionArgs,
                                                                   null,
                                                                   null,
                                                                   sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);  // <-- allows CursorLoader to auto reload
        return cursor;
    }
}
