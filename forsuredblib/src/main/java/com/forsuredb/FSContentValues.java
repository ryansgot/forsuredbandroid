package com.forsuredb;


import android.content.ContentValues;

import com.forsuredb.api.RecordContainer;

/**
 * <p>
 *     This is a ContentValues object wrapper that implements RecordContainer so that a FSSaveApi can be created
 * </p>
 */
public class FSContentValues implements RecordContainer {

    private final ContentValues cv;

    private FSContentValues(ContentValues cv) {
        this.cv = cv;
    }

    public static FSContentValues getNew() {
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
