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
 *     The result of a call to {@link FSSaveApi#save() FSSaveApi.save()}
 * </p>
 * @param <U> The resource locator, same as {@link FSSaveApi FSSaveApi}
 */
public interface SaveResult<U> {

    /**
     * @return If an error occurred, the {@link Exception Exception} object
     */
    Exception exception();

    /**
     * @return a resource locator if a record was inserted
     */
    U inserted();

    /**
     * @return the number of rows affected
     */
    int rowsAffected();
}
