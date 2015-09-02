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

import android.content.ContentValues;

import com.forsuredb.api.RecordContainer;

/**
 * <p>
 *     This is a ContentValues object wrapper that implements RecordContainer so that a FSSaveApi can be created
 * </p>
 */
/*package*/ class FSContentValues implements RecordContainer {

    private final ContentValues cv;

    private FSContentValues(ContentValues cv) {
        this.cv = cv;
    }

    /*package*/ static FSContentValues getNew() {
        return new FSContentValues(new ContentValues());
    }

    @Override
    public String toString() {
        return cv.toString();
    }

    @Override
    public boolean equals(Object o) {
        return cv.equals(o);
    }

    @Override
    public int hashCode() {
        return cv.hashCode();
    }

    @Override
    public Object get(String column) {
        return cv.get(column);
    }

    @Override
    public void put(String column, String value) {
        cv.put(column, value);
    }

    @Override
    public void put(String column, long value) {
        cv.put(column, value);
    }

    @Override
    public void put(String column, int value) {
        cv.put(column, value);
    }

    @Override
    public void put(String column, double value) {
        cv.put(column, value);
    }

    @Override
    public void put(String column, byte[] value) {
        cv.put(column, value);
    }

    @Override
    public void clear() {
        cv.clear();
    }

    public ContentValues getContentValues() {
        return cv;
    }
}
