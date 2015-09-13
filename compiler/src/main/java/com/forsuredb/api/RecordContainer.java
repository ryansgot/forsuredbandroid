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
package com.forsuredb.api;

/**
 * <p>
 *     Contains a Record that is yet to be inserted or updated in the database. This <i>IS NOT</i> a
 *     record that has been retrieved from the database.
 * </p>
 */
public interface RecordContainer {

    /**
     * <p>
     *     Use this method if you want to check a value that you have put into the container.
     * </p>
     * @param column The name of the column for which data is stored in this container
     * @return null if one of the put methods was not previously called for this column; the Object
     * that was stored if one of the put methods was previously called for this column
     */
    Object get(String column);
    void put(String column, String value);
    void put(String column, long value);
    void put(String column, int value);
    void put(String column, double value);
    void put(String column, byte[] value);

    /**
     * <p>
     *     Empty all storage of this RecordContainer
     * </p>
     */
    void clear();
}
