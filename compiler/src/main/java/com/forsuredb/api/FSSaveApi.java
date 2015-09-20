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
 *     All {@link FSGetApi FSGetApi} extensions defined by the user will have a corresponding
 *     FSSaveApi extension.
 * </p>
 * @param <U> Although it is not enforced, This parameter should be a Uri or some other resource
 *           locator
 * @author Ryan Scott
 */
public interface FSSaveApi<U> {

   /**
    * <p>
    *     Performs either an insertion or an update of the fields you have set. The operation
    *     will be an . . .
    * </p>
    * <ul>
    *     <li>
    *         Insertion if you either did not set any selection criteria or the search criteria
    *         you specified matches no records.
    *     </li>
    *     <li>
    *         Update if you both set selection criteria and that criteria matches at least one
    *         record.
    *     </li>
    * </ul>
    * @return A descriptor of the result of the save operation
    * @see FSSaveAdapter
    */
    SaveResult<U> save();

    /**
     * <p>
     *     Attempts an update to the database. However, any fields that you've set prior to
     *     calling this method will not be saved.
     * </p>
     * @return A descriptor of the result of the softDelete operation
     * @see FSSaveAdapter
     */
    SaveResult<U> softDelete();

    /**
     * <p>
     *     A hard delete actually deletes the record(s) from the database. If there is a
     *     foreign key pointing to any of the matching records, then the
     *     {@link com.forsuredb.annotation.ForeignKey.ChangeAction} you will be executed.
     * </p>
     * @return the number of rows deleted
     * @see FSSaveAdapter
     */
    int hardDelete();
}
