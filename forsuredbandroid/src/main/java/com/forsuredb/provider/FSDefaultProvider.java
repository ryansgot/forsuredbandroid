package com.forsuredb.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.forsuredb.ForSure;

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
        if (!ForSure.canResolve(uri)) {
            return null;
        }
        return null;    // TODO: resolve this;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (!ForSure.canResolve(uri)) {
            return null;
        }

        final String tableName = ForSure.resolve(uri).table().getName();
        long rowId = ForSure.getWritableDatabase().insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (rowId != -1) {
            final Uri insertedItemUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(insertedItemUri, null);
            return insertedItemUri;
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (!ForSure.canResolve(uri)) {
            return 0;
        }

        final String tableName = ForSure.resolve(uri).table().getName();
        final QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        final int rowsAffected = ForSure.getWritableDatabase().update(tableName, values, qc.getSelection(), qc.getSelectionArgs());
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (!ForSure.canResolve(uri)) {
            return 0;
        }

        final String tableName = ForSure.resolve(uri).table().getName();
        final QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        final int rowsAffected = ForSure.getWritableDatabase().delete(tableName, qc.getSelection(), qc.getSelectionArgs());
        if (rowsAffected != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsAffected;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (!ForSure.canResolve(uri)) {
            return null;
        }

        final String tableName = ForSure.resolve(uri).table().getName();
        final QueryCorrector qc = new QueryCorrector(uri, selection, selectionArgs);
        final Cursor cursor = ForSure.getReadableDatabase().query(tableName, projection, qc.getSelection(), qc.getSelectionArgs(), null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);  // <-- allows CursorLoader to auto reload
        return cursor;
    }
}
