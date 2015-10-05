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

import com.forsuredb.TestData;
import com.forsuredb.migration.QueryGenerator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class DiffGeneratorTest {

    private DiffGenerator diffGeneratorUnderTest;

    private TableContext migrationContext;
    private TableContext processingContext;
    private String[] expectedSql;

    public DiffGeneratorTest(TableContext migrationContext, TableContext processingContext, String[] expectedSql) {
        this.migrationContext = migrationContext;
        this.processingContext = processingContext;
        this.expectedSql = expectedSql;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // The processing context has a table that the migration context does not have
                {
                        TestData.newTableContext()
                                .build(),
                        TestData.newTableContext()
                                .addTable(TestData.table().build())
                                .build(),
                        new String[] {
                                "CREATE TABLE " + TestData.TABLE_NAME + "(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT CURRENT_TIMESTAMP, deleted INTEGER DEFAULT 0, modified DATETIME DEFAULT CURRENT_TIMESTAMP);",
                                "CREATE TRIGGER " + TestData.TABLE_NAME + "_updated_trigger AFTER UPDATE ON " + TestData.TABLE_NAME + " BEGIN UPDATE " + TestData.TABLE_NAME + " SET modified=CURRENT_TIMESTAMP WHERE _id=NEW._id; END;"
                        }
                },
                // The processing context has a column that the migration context does not have
                {
                        TestData.newTableContext()
                                .addTable(TestData.table().build())
                                .build(),
                        TestData.newTableContext()
                                .addTable(TestData.table()
                                        .addColumn(TestData.bigDecimalCol()
                                                .build())
                                        .build())
                                .build(),
                        new String[] {
                                "ALTER TABLE " + TestData.TABLE_NAME + " ADD COLUMN big_decimal_column REAL;"
                        }
                },
                // The processing context has a foreign key the migration context does not know about (default delete and update actions)
                {
                        TestData.newTableContext().addTable(TestData.table().build()).build(),
                        TestData.newTableContext()
                                .addTable(TestData.table()
                                        .addColumn(TestData.longCol()
                                                .foreignKey(TestData.cascadeFKI("user").build())
                                                .build())
                                        .build())
                                .build(),
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TestData.TABLE_NAME + " AS SELECT _id, created, deleted, modified FROM " + TestData.TABLE_NAME + ";",
                                "DROP TABLE IF EXISTS " + TestData.TABLE_NAME + ";",
                                "CREATE TABLE " + TestData.TABLE_NAME + "(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT CURRENT_TIMESTAMP, deleted INTEGER DEFAULT 0, modified DATETIME DEFAULT CURRENT_TIMESTAMP, long_column INTEGER, FOREIGN KEY(long_column) REFERENCES user(_id) ON UPDATE CASCADE ON DELETE CASCADE);",
                                "CREATE TRIGGER " + TestData.TABLE_NAME + "_updated_trigger AFTER UPDATE ON " + TestData.TABLE_NAME + " BEGIN UPDATE " + TestData.TABLE_NAME + " SET modified=CURRENT_TIMESTAMP WHERE _id=NEW._id; END;",
                                "INSERT INTO " + TestData.TABLE_NAME + " SELECT _id, created, deleted, modified, null AS long_column FROM temp_" + TestData.TABLE_NAME + ";",
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";"
                        }
                },
                // The processing context has a unique index the migration context doesn't know about
                {
                        TestData.newTableContext()
                                .addTable(TestData.table().build())
                                .build(),
                        TestData.newTableContext()
                                .addTable(TestData.table()
                                        .addColumn(TestData.stringCol().unique(true).build())
                                        .build())
                                .build(),
                        new String[] {
                                "ALTER TABLE " + TestData.TABLE_NAME + " ADD COLUMN string_column TEXT;",
                                "CREATE UNIQUE INDEX " + TestData.TABLE_NAME + "_string_column ON " + TestData.TABLE_NAME + "(string_column);"
                        }
                },
                // The processing context has a unique index on a column the migration context knows about, but doesn't know is unique
                {
                        TestData.newTableContext()
                                .addTable(TestData.table()
                                        .addColumn(TestData.stringCol().build())
                                        .build())
                                .build(),
                        TestData.newTableContext()
                                .addTable(TestData.table()
                                        .addColumn(TestData.stringCol().unique(true).build())
                                        .build())
                                .build(),
                        new String[] {
                                "CREATE UNIQUE INDEX " + TestData.TABLE_NAME + "_string_column ON " + TestData.TABLE_NAME + "(string_column);"
                        }
                }
        });
    }

    @Before
    public void setUp() {
        diffGeneratorUnderTest = new DiffGenerator(migrationContext);
    }

    @Test
    public void shouldHaveCorrectNumberOfQueries() {
        PriorityQueue<QueryGenerator> queryGenerators = diffGeneratorUnderTest.analyzeDiff(processingContext);
        String[] generatedSql = getGeneratedSqlFrom(queryGenerators);
        assertEquals(expectedSql.length, generatedSql.length);
    }

    @Test
    public void shouldMatchQueriesInOrderAndContent() {
        PriorityQueue<QueryGenerator> queryGenerators = diffGeneratorUnderTest.analyzeDiff(processingContext);
        String[] generatedSql = getGeneratedSqlFrom(queryGenerators);
        for (int i = 0; i < expectedSql.length; i++) {
            assertEquals(expectedSql[i], generatedSql[i]);
        }
    }

    private String[] getGeneratedSqlFrom(PriorityQueue<QueryGenerator> queryGenerators) {
        List<String> retList = new ArrayList<>();
        while (queryGenerators.size() > 0) {
            retList.addAll(queryGenerators.remove().generate());
        }
        return retList.toArray(new String[retList.size()]);
    }

}
