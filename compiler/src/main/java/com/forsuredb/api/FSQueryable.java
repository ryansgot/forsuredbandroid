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

public interface FSQueryable<U, R extends RecordContainer> {
    /**
     * @param recordContainer
     * @return if you've parameterized this properly, U would be a class that locates records, and the return would be the locator
     * for the record that was inserted
     */
    U insert(R recordContainer);

    /**
     * @param recordContainer A container for the record to be updated
     * @param selection
     * @return the number of records affected by the update
     */
    int update(R recordContainer, FSSelection selection);

    /**
     * @param selection
     * @return the number of records affected by the delete
     */
    int delete(FSSelection selection);

    /**
     * @param projection
     * @param selection
     * @param sortOrder the SQL sort order for the query
     * @return A Retriever that will be able to retrieve records returned by this query
     */
    Retriever query(FSProjection projection, FSSelection selection, String sortOrder);
}
