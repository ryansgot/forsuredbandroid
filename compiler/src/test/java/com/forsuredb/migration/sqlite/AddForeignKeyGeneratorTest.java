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
public class AddForeignKeyGeneratorTest extends BaseSQLiteGeneratorTest {

    private AddForeignKeyGenerator generatorUnderTest;

    private TableInfo table;
    private ColumnInfo column;

    public AddForeignKeyGeneratorTest(TableInfo table, ColumnInfo column, String... expectedSql) {
        super (expectedSql);
        this.table = table;
        this.column = column;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Add a foreign key to a basic table with no extra columns
                {
                        TestData.table().addColumn(TestData.longCol().foreignKeyTableName("user").foreignKey(true).foreignKeyColumnName("_id").build())
                                .build(),
                        TestData.longCol().foreignKeyTableName("user").foreignKey(true).foreignKeyColumnName("_id").build(),
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TestData.TABLE_NAME + " AS SELECT _id, created, deleted, modified FROM " + TestData.TABLE_NAME + ";",
                                "DROP TABLE IF EXISTS " + TestData.TABLE_NAME + ";",
                                "CREATE TABLE " + TestData.TABLE_NAME + "(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT CURRENT_TIMESTAMP, deleted INTEGER DEFAULT 0, modified DATETIME DEFAULT CURRENT_TIMESTAMP, long_column INTEGER, FOREIGN KEY(long_column) REFERENCES user(_id));",
                                "CREATE TRIGGER " + TestData.TABLE_NAME + "_updated_trigger AFTER UPDATE ON " + TestData.TABLE_NAME + " BEGIN UPDATE " + TestData.TABLE_NAME + " SET modified=CURRENT_TIMESTAMP WHERE _id=NEW._id; END;",
                                "INSERT INTO " + TestData.TABLE_NAME + " SELECT _id, created, deleted, modified, null AS long_column FROM temp_" + TestData.TABLE_NAME + ";",
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";"
                        }
                },
                // Add a foreign key to a basic table with one extra non-foreign key column
                {
                        TestData.table().addColumn(TestData.longCol().foreignKeyTableName("user").foreignKey(true).foreignKeyColumnName("_id").build())
                                .addColumn(TestData.intCol().build())
                                .build(),
                        TestData.longCol().foreignKeyTableName("user").foreignKey(true).foreignKeyColumnName("_id").build(),
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TestData.TABLE_NAME + " AS SELECT _id, created, deleted, modified, int_column FROM " + TestData.TABLE_NAME + ";",
                                "DROP TABLE IF EXISTS " + TestData.TABLE_NAME + ";",
                                "CREATE TABLE " + TestData.TABLE_NAME + "(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT CURRENT_TIMESTAMP, deleted INTEGER DEFAULT 0, modified DATETIME DEFAULT CURRENT_TIMESTAMP, long_column INTEGER, FOREIGN KEY(long_column) REFERENCES user(_id));",
                                "CREATE TRIGGER " + TestData.TABLE_NAME + "_updated_trigger AFTER UPDATE ON " + TestData.TABLE_NAME + " BEGIN UPDATE " + TestData.TABLE_NAME + " SET modified=CURRENT_TIMESTAMP WHERE _id=NEW._id; END;",
                                "ALTER TABLE " + TestData.TABLE_NAME + " ADD COLUMN int_column INTEGER;",
                                "INSERT INTO " + TestData.TABLE_NAME + " SELECT _id, created, deleted, modified, null AS long_column, int_column FROM temp_" + TestData.TABLE_NAME + ";",
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";"
                        }
                },
                // Add a foreign key to a basic table with one extra foreign key
                {
                        TestData.table().addColumn(TestData.longCol().foreignKeyTableName("user").foreignKey(true).foreignKeyColumnName("_id").build())
                                .addColumn(TestData.intCol().foreignKey(true).foreignKeyColumnName("_id").foreignKeyTableName("profile_info").build())
                                .build(),
                        TestData.longCol().foreignKeyTableName("user").foreignKey(true).foreignKeyColumnName("_id").build(),
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TestData.TABLE_NAME + " AS SELECT _id, created, deleted, modified, int_column FROM " + TestData.TABLE_NAME + ";",
                                "DROP TABLE IF EXISTS " + TestData.TABLE_NAME + ";",
                                "CREATE TABLE " + TestData.TABLE_NAME + "(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT CURRENT_TIMESTAMP, deleted INTEGER DEFAULT 0, modified DATETIME DEFAULT CURRENT_TIMESTAMP, int_column INTEGER, long_column INTEGER, FOREIGN KEY(int_column) REFERENCES profile_info(_id), FOREIGN KEY(long_column) REFERENCES user(_id));",
                                "CREATE TRIGGER " + TestData.TABLE_NAME + "_updated_trigger AFTER UPDATE ON " + TestData.TABLE_NAME + " BEGIN UPDATE " + TestData.TABLE_NAME + " SET modified=CURRENT_TIMESTAMP WHERE _id=NEW._id; END;",
                                "INSERT INTO " + TestData.TABLE_NAME + " SELECT _id, created, deleted, modified, int_column, null AS long_column FROM temp_" + TestData.TABLE_NAME + ";",
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";"
                        }
                }
        });
    }

    @Before
    public void setUp() {
        generatorUnderTest = new AddForeignKeyGenerator(table, column);
    }

    @Override
    protected QueryGenerator getGenerator() {
        return generatorUnderTest;
    }
}
