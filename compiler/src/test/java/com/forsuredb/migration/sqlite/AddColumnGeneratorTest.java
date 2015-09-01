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
import com.forsuredb.migration.QueryGenerator;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class AddColumnGeneratorTest extends BaseSQLiteGeneratorTest {

    private AddColumnGenerator generatorUndertest;

    private ColumnInfo column;

    public AddColumnGeneratorTest(ColumnInfo column, String... expectedSql) {
        super(expectedSql);
        this.column = column;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // add a normal column
                {
                        TestData.stringCol().build(),
                        new String[] {
                                "ALTER TABLE " + TestData.TABLE_NAME + " ADD COLUMN string_column TEXT;"
                        }
                },
                // add a column that has a default set
                {
                        TestData.dateCol().defaultValue("CURRENT_TIMESTAMP").build(),
                        new String[] {
                                "ALTER TABLE " + TestData.TABLE_NAME + " ADD COLUMN date_column DATETIME DEFAULT CURRENT_TIMESTAMP;"
                        }
                }
        });
    }

    @Override
    protected QueryGenerator getGenerator() {
        return generatorUndertest;
    }

    @Before
    public void setUp() {
        generatorUndertest = new AddColumnGenerator(TestData.TABLE_NAME, column);
    }
}
