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

import java.util.List;

/**
 * <p>
 *     if you've parameterized your FSQueryable properly, U would be a class that locates records
 * </p>
 * @param <U> The class used to locate records in your database
 * @param <R> an extension of {@link RecordContainer}, which contains a record before it is inserted/updated in the database
 */
public interface FSQueryable<U, R extends RecordContainer> {

    /**
     * @param recordContainer An extension of {@link RecordContainer} which contains the record to be inserted
     * @return if you've {@link FSQueryable parameterized this class correctly}, then a record
     * locator for the inserted record
     */
    U insert(R recordContainer);

    /**
     * @param recordContainer An extension of {@link RecordContainer} which contains the record to be updated
     * @param selection The {@link FSSelection} that defines the subset of records to update
     * @return the number of records affected by the update
     */
    int update(R recordContainer, FSSelection selection);

    /**
     * @param selection The {@link FSSelection} that defines the subset of records to delete
     * @return the number of records affected by the delete
     */
    int delete(FSSelection selection);

    /**
     * @param projection The {@link FSProjection} that defines the subest of columns to retrieve for each record
     * @param selection The {@link FSSelection} that defines the subset of records to retrieve
     * @param sortOrder the SQL sort order for the query
     * @return A Retriever that will be able to retrieve records returned by this query
     */
    Retriever query(FSProjection projection, FSSelection selection, String sortOrder);

    /**
     * @param joins A list of {@link FSJoin} describing how to join
     * @param projections The list of {@link FSProjection} that defines the columns to return in the SELECT query
     * @param selection The {@link FSSelection} that defines the subset of records to retrieve
     * @param sortOrder the SQL sort order for the query
     * @return A Retriever that will be able to retrieve records returned by this join query
     */
    Retriever query(List<FSJoin> joins, List<FSProjection> projections, FSSelection selection, String sortOrder);
}
