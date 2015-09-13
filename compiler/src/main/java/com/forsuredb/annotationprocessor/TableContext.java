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
package com.forsuredb.annotationprocessor;

import java.util.Collection;

/**
 * <p>
 *     Describes a collection of tables.
 * </p>
 * @author Ryan Scott
 */
public interface TableContext {

    /**
     * @param tableName the name of the table to check
     * @return true if the table exists within the context
     */
    boolean hasTable(String tableName);

    /**
     * @param tableName the name of the table to get
     * @return a TableInfo object if the context contains the table and null if not
     */
    TableInfo getTable(String tableName);

    /**
     * @return all of the tables in the context
     */
    Collection<TableInfo> allTables();
}
