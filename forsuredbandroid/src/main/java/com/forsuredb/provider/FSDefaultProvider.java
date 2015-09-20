package com.forsuredb.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.forsuredb.FSDBHelper;
import com.forsuredb.ForSureAndroidInfoFactory;

/**
 * <p>
 *     The default content provider that integrates with ForSure
 * </p>
 * @author Ryan Scott
 */
public class FSDefaultProvider extends ContentProvider {

    private ForSureAndroidInfoFactory infoFactory;

    public FSDefaultProvider() {}

    @Override
    public boolean onCreate() {
        infoFactory = new ForSureAndroidInfoFactory(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return "vnd.android.cursor/" + infoFactory.tableName(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final String tableName = infoFactory.tableName(uri);
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
        final String tableName = infoFactory.tableName(uri);
        final QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        final int rowsAffected = FSDBHelper.inst().getWritableDatabase().update(tableName, values, qc.getSelection(), qc.getSelectionArgs());
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final String tableName = infoFactory.tableName(uri);
        final QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        final int rowsAffected = FSDBHelper.inst().getWritableDatabase().delete(tableName, qc.getSelection(), qc.getSelectionArgs());
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final String tableName = infoFactory.tableName(uri);
        final QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        final Cursor cursor = FSDBHelper.inst().getReadableDatabase().query(tableName, projection, qc.getSelection(), qc.getSelectionArgs(), null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);  // <-- allows CursorLoader to auto reload
        return cursor;
    }
}
