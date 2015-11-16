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

public interface Resolver<U, G extends FSGetApi, S extends FSSaveApi<U>, F extends Finder<U, G, S, F>> {

    /**
     * <p>
     *     Get the api that can return fields associated with the table this Resolver can resolve
     * </p>
     * @return
     */
    G getApi();

    /**
     * <p>
     *     This version resets the query parameters so that the same Resolver may be used to build a
     *     new, different query.
     * </p>
     * @return A {@link Retriever} that can retrieve records and fields associated with the query built up
     */
    Retriever get();

    /**
     * <p>
     *     just like {@link #get()} except that it preserves the query parameters so that you can get
     *     the same {@link Retriever} again without rebuilding the query.
     * </p>
     * @return A {@link Retriever} that can retrieve records and fields associated with the query built up
     * so far
     * @see #get()
     */
    Retriever preserveQueryStateAndGet();

    /**
     * <p>
     *     If you set any query parameters by calling {@link #find()}, then the returned {@link FSSaveApi}
     *     extension will try to update the matching records. Otherwise, the returned {@link FSSaveApi} will
     *     try to insert records.
     * </p>
     * @return {@link S} extension of {@link FSSaveApi}
     */
    S set();

    /**
     * <p>
     *      This finder will be reset if you call {@link #get()} instead of {@link #preserveQueryStateAndGet()}.
     * </p>
     * @return {@link Finder} that specifically allows for narrowing the results of the query.
     */
    F find();

    /**
     * @return The {@link U} locator for the entire table that this resolver resolves
     */
    U tableLocator();

    /**
     * @param id the id field of the record. This does not guarantee the record exists.
     * @return The {@link U} locator for a record in the table this resolver resolves with the id passed in
     */
    U recordLocator(long id);

    /**
     * <p>
     *     This represents all of the join operations that have been performed on the resolver up to this
     *     point. But if you call {@link #get()} instead of {@link #preserveQueryStateAndGet()}, then the
     *     {@link U} will be reset.
     * </p>
     * @return the currently built-up locator.
     */
    U currentLocator();
}
