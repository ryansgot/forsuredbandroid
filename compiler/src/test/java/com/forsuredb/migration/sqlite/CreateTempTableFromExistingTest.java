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
import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.annotationprocessor.TableInfo;
import com.forsuredb.migration.QueryGenerator;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class CreateTempTableFromExistingTest extends BaseSQLiteGeneratorTest {

    private CreateTempTableFromExisting generatorUnderTest;

    private TableInfo table;
    private ColumnInfo[] excludedColumns;

    public CreateTempTableFromExistingTest(TableInfo table, ColumnInfo[] excludedColumns, String[] expectedSql) {
        super(expectedSql);
        this.table = table;
        this.excludedColumns = excludedColumns;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // Copy a table with a non-default column
                {
                        TestData.table().addColumn(TestData.stringCol().build()).build(),
                        new ColumnInfo[] {},
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TestData.TABLE_NAME + " AS SELECT _id, created, deleted, modified, string_column FROM " + TestData.TABLE_NAME + ";"
                        }
                },
                // Copy a table with a foreign key column
                {
                        TestData.table().addColumn(TestData.longCol().foreignKey(TestData.cascadeFKI("user").build()).build()).build(),
                        new ColumnInfo[] {},
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TestData.TABLE_NAME + " AS SELECT _id, created, deleted, modified, long_column FROM " + TestData.TABLE_NAME + ";"
                        }
                },
                // Copy a table with an excluded column
                {
                        TestData.table().addColumn(TestData.longCol().foreignKey(TestData.cascadeFKI("user").build()).build()).build(),
                        new ColumnInfo[] {
                                TestData.longCol().foreignKey(TestData.cascadeFKI("user").build()).build()
                        },
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TestData.TABLE_NAME + " AS SELECT _id, created, deleted, modified FROM " + TestData.TABLE_NAME + ";"
                        }
                },
        });
    }

    @Before
    public void setUp() {
        generatorUnderTest = new CreateTempTableFromExisting(table, excludedColumns);
    }

    @Override
    protected QueryGenerator getGenerator() {
        return generatorUnderTest;
    }
}
