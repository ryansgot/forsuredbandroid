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

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.PrimaryKey;

import java.util.Date;

/**
 * <p>
 *     The parent interface for all table definitions. You <i>MUST</i> extend this interface and
 *     annotate your extension with the {@link com.forsuredb.annotation.FSTable FSTable} annotation.
 * </p>
 * <p>
 *     Note that the methods here correspond directly to the columns defined in
 *     {@link com.forsuredb.annotationprocessor.TableInfo#DEFAULT_COLUMNS TableInfo.DEFAULT_COLUMNS}
 *     .
 * </p>
 * @author Ryan Scott
 */
public interface FSGetApi {

    /**
     * <p>
     *     Defines an integer primary key for each table
     * </p>
     * @param retriever
     * @return the id of the record
     */
    @FSColumn("_id") @PrimaryKey long id(Retriever retriever);

    /**
     * @param retriever
     * @return the Date this record was created
     */
    @FSColumn("created") Date created(Retriever retriever);

    /**
     * @param retriever
     * @return the Date this record was last modified
     */
    @FSColumn("modified") Date modified(Retriever retriever);

    /**
     * @param retriever
     * @return true if the record is deleted, false if not
     */
    @FSColumn("deleted") boolean deleted(Retriever retriever);
}
