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
package com.forsuredb.migration.sqlite;

import com.forsuredb.TestData;
import com.forsuredb.migration.QueryGenerator;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class QueryGeneratorFactoryTest {

    @Test
    public void shouldCreateCreateTableQueryGeneratorInstance() {
        QueryGenerator qg = QueryGeneratorFactory.createForTable(TestData.table().build());
        assertTrue("createForTable method did not return an instance of the CreateTableQueryGenerator class", qg instanceof CreateTableGenerator);
    }

    @Test
    public void shouldCreateAddColumnGeneratorForNonUniqueColumn() {
        QueryGenerator qg = QueryGeneratorFactory.createForColumn(TestData.table().build(), TestData.intCol().build());
        assertTrue("When column was nonUnique, the returned query generator was not an instance of AddColumnGenerator", qg instanceof AddColumnGenerator);
    }

    @Test
    public void shouldCreateAddUniqueColumnGeneratorForUniqueColumn() {
        QueryGenerator qg = QueryGeneratorFactory.createForColumn(TestData.table().build(), TestData.intCol().unique(true).build());
        assertTrue("When column was unique, the returned query generator was not an instance of AddUniqueColumnGenerator", qg instanceof AddUniqueColumnGenerator);
    }

    @Test
    public void shouldCreateAddColumnGeneratorForForeignKeyColumn() {
        QueryGenerator qg = QueryGeneratorFactory.createForColumn(TestData.table().build(), TestData.intCol().foreignKey(TestData.defaultFKI("user").build()).build());
        assertTrue("When column was foreign key, the returned query generator was not an instance of AddForeignKeyGenerator", qg instanceof AddForeignKeyGenerator);
    }
}
