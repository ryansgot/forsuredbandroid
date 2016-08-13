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
package com.fsryan.fosuredb.provider;

import android.content.ContentValues;

import com.fsryan.forsuredb.api.RecordContainer;

/**
 * <p>
 *     This is a wrapper for a {@link ContentValues} object. Because it implements
 *     {@link RecordContainer}, it can be used to create an
 *     {@link com.fsryan.forsuredb.api.FSSaveApi FSSaveApi}.
 * </p>
 * @author Ryan Scott
 */
public class FSContentValues implements RecordContainer {

    private final ContentValues cv;

    /**
     * <p>
     *     The reference to the {@link ContentValues} object reference cannot be changed, but
     *     you can do all of the normal {@link ContentValues} operations on an
     *     {@link FSContentValues}.
     * </p>
     * @param cv The {@link ContentValues} object to wrap
     */
    private FSContentValues(ContentValues cv) {
        this.cv = cv;
    }

    /**
     * <p>
     *     This method enforces that {@link FSContentValues} are constructed with an empty
     *     {@link ContentValues} object. It is easier to manage state this way.
     * </p>
     * @return a new empty {@link FSContentValues}.
     */
    public static FSContentValues getNew() {
        return new FSContentValues(new ContentValues());
    }

    /**
     * @see ContentValues#toString()
     */
    @Override
    public String toString() {
        return cv.toString();
    }

    /**
     * @see ContentValues#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        return cv.equals(o);
    }

    /**
     * @see ContentValues#hashCode()
     */
    @Override
    public int hashCode() {
        return cv.hashCode();
    }

    /**
     * @see ContentValues#get(String)
     */
    @Override
    public Object get(String column) {
        return cv.get(column);
    }

    /**
     * @see ContentValues#put(String, String)
     */
    @Override
    public void put(String column, String value) {
        cv.put(column, value);
    }

    /**
     * @see ContentValues#put(String, Long)
     */
    @Override
    public void put(String column, long value) {
        cv.put(column, value);
    }

    /**
     * @see ContentValues#put(String, Integer)
     */
    @Override
    public void put(String column, int value) {
        cv.put(column, value);
    }

    /**
     * @see ContentValues#put(String, Double)
     */
    @Override
    public void put(String column, double value) {
        cv.put(column, value);
    }

    /**
     * @see ContentValues#put(String, byte[])
     */
    @Override
    public void put(String column, byte[] value) {
        cv.put(column, value);
    }

    /**
     * @see ContentValues#clear()
     */
    @Override
    public void clear() {
        cv.clear();
    }

    /**
     * <p>
     *     Any changes you make to the wrapped {@link ContentValues} object will carry over
     *     to the {@link FSContentValues} object that wraps it.
     * </p>
     * @return the wrapped {@link ContentValues}
     */
    public ContentValues getContentValues() {
        return cv;
    }
}
