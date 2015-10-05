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
package com.forsuredb.cursor;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * <p>
 *     Wraps a {@link Cursor}, getting its implementation from {@link CursorWrapper}
 *     and implements {@link com.forsuredb.api.Retriever} so that it may be used as the parameter to
 *     {@link com.forsuredb.api.FSGetApi FSGetApi} methods.
 * </p>
 */
public class FSCursor extends CursorWrapper implements com.forsuredb.api.Retriever {

    public FSCursor(Cursor cursor) {
        super(cursor);
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
}
