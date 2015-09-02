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
package com.forsuredb;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.forsuredb.api.Retriever;

/**
 * <p>
 *     This is a very simple wrapper for a cursor that implements the Retriever interface for Android compatibility
 * </p>
 */
public class FSCursor implements Retriever, Cursor {

    private final Cursor cursor;

    public FSCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public String getString(String column) {
        return getString(getColumnIndex(column));
    }

    @Override
    public int getInt(String column) {
        return getInt(getColumnIndex(column));
    }

    @Override
    public long getLong(String column) {
        return getLong(getColumnIndex(column));
    }

    @Override
    public double getDouble(String column) {
        return getDouble(getColumnIndex(column));
    }

    @Override
    public byte[] getBlob(String column) {
        return getBlob(getColumnIndex(column));
    }

    @Override
    public int getCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    @Override
    public int getPosition() {
        return cursor == null ? -1 : cursor.getPosition();
    }

    @Override
    public boolean move(int i) {
        return cursor != null && cursor.move(i);
    }

    @Override
    public boolean moveToPosition(int position) {
        return cursor != null && cursor.moveToPosition(position);
    }

    @Override
    public boolean moveToFirst() {
        return cursor != null && cursor.moveToFirst();
    }

    @Override
    public boolean moveToLast() {
        return cursor != null && cursor.moveToLast();
    }

    @Override
    public boolean moveToNext() {
        return cursor != null && cursor.moveToNext();
    }

    @Override
    public boolean moveToPrevious() {
        return cursor != null && cursor.moveToPrevious();
    }

    @Override
    public boolean isFirst() {
        return cursor != null && cursor.isFirst();
    }

    @Override
    public boolean isLast() {
        return cursor != null && cursor.isLast();
    }

    @Override
    public boolean isBeforeFirst() {
        return cursor != null && cursor.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() {
        return cursor != null && cursor.isAfterLast();
    }

    @Override
    public int getColumnIndex(String column) {
        return cursor == null ? -1 : cursor.getColumnIndex(column);
    }

    @Override
    public int getColumnIndexOrThrow(String column) throws IllegalArgumentException {
        return cursor == null ? -1 : cursor.getColumnIndexOrThrow(column);
    }

    @Override
    public String getColumnName(int i) {
        return cursor == null ? null : cursor.getColumnName(i);
    }

    @Override
    public String[] getColumnNames() {
        return cursor == null ? null : cursor.getColumnNames();
    }

    @Override
    public int getColumnCount() {
        if (cursor == null) {
            throw new IllegalStateException("Cannot getColumnCount on a null cursor");
        }
        return cursor.getColumnCount();
    }

    @Override
    public byte[] getBlob(int i) {
        if (cursor == null) {
            throw new IllegalStateException("Cannot getString on a null cursor");
        }
        return cursor.getBlob(i);
    }

    @Override
    public String getString(int i) {
        if (cursor == null) {
            throw new IllegalStateException("Cannot getString on a null cursor");
        }
        return cursor.getString(i);
    }

    @Override
    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
        if (cursor == null) {
            throw new IllegalStateException("Cannot copyStringToBuffer on a null cursor");
        }
        cursor.copyStringToBuffer(i, charArrayBuffer);
    }

    @Override
    public short getShort(int i) {
        if (cursor == null) {
            throw new IllegalStateException("Cannot getShort on a null cursor");
        }
        return cursor.getShort(i);
    }

    @Override
    public int getInt(int i) {
        if (cursor == null) {
            throw new IllegalStateException("Cannot getInt on a null cursor");
        }
        return cursor.getInt(i);
    }

    @Override
    public long getLong(int i) {
        if (cursor == null) {
            throw new IllegalStateException("Cannot getLong on a null cursor");
        }
        return cursor.getLong(i);
    }

    @Override
    public float getFloat(int i) {
        if (cursor == null) {
            throw new IllegalStateException("Cannot getFloat on a null cursor");
        }
        return cursor.getFloat(i);
    }

    @Override
    public double getDouble(int i) {
        if (cursor == null) {
            throw new IllegalStateException("Cannot getDouble on a null cursor");
        }
        return cursor.getDouble(i);
    }

    @Override
    public int getType(int i) {
        if (cursor == null) {
            throw new IllegalStateException("Cannot getType on a null cursor");
        }
        return cursor.getType(i);
    }

    @Override
    public boolean isNull(int i) {
        return cursor == null || cursor.isNull(i);
    }

    @Override
    @Deprecated
    public void deactivate() {
        if (cursor == null) {
            return;
        }
        cursor.deactivate();
    }

    @Override
    @Deprecated
    public boolean requery() {
        return cursor != null && cursor.requery();
    }

    @Override
    public void close() {
        if (cursor == null) {
            return;
        }
        cursor.close();
    }

    @Override
    public boolean isClosed() {
        return cursor == null || cursor.isClosed();
    }

    @Override
    public void registerContentObserver(ContentObserver contentObserver) {
        if (cursor == null) {
            return;
        }
        cursor.registerContentObserver(contentObserver);
    }

    @Override
    public void unregisterContentObserver(ContentObserver contentObserver) {
        if (cursor == null) {
            return;
        }
        cursor.unregisterContentObserver(contentObserver);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        if (cursor == null) {
            return;
        }
        cursor.registerDataSetObserver(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        if (cursor == null) {
            return;
        }
        cursor.unregisterDataSetObserver(dataSetObserver);
    }

    @Override
    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {
        if (cursor == null) {
            return;
        }
        cursor.setNotificationUri(contentResolver, uri);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public Uri getNotificationUri() {
        return cursor == null ? null : cursor.getNotificationUri();
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        return cursor != null && cursor.getWantsAllOnMoveCalls();
    }

    @Override
    public Bundle getExtras() {
        return cursor == null ? null : cursor.getExtras();
    }

    @Override
    public Bundle respond(Bundle bundle) {
        return cursor == null ? null : cursor.respond(bundle);
    }
}
