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
    * @return A descriptor of the result of the save operation
    */
    SaveResult<U> save();

    /**
     * <p>
     *     A soft delete flips the deleted flag on the record to true.
     * </p>
     * @return A descriptor of the result of the softDelete operation
     */
    SaveResult<U> softDelete();

    /**
     * <p>
     *     A hard delete actually deletes the record
     * </p>
     * @param selection The selection of rows to delete
     * @return the number of rows deleted
     */
    int hardDelete(FSSelection selection);
}
