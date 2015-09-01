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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TableInfoTest {

    private TableInfo tableUnderTest;

    private List<ColumnInfo> nonDefaultColumns;

    public TableInfoTest(ColumnInfo[] nonDefaultColumns) {
        this.nonDefaultColumns = createColumns(nonDefaultColumns);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Table with only default columns
                {
                        new ColumnInfo[] {}
                },
                {
                        new ColumnInfo[] {TestData.longCol().build()}
                },
        });
    }

    @Before
    public void setUp() {
        TableInfo.Builder builder = TestData.table();
        for (ColumnInfo column : nonDefaultColumns) {
            builder.addColumn(column);
        }
        tableUnderTest = builder.build();
    }

    @Test
    public void shouldHaveDefaultColumns() {
        for (ColumnInfo column : TestData.DEFAULT_COLUMNS) {
            assertTrue("Default column: " + column.getColumnName() + " was not in table", tableUnderTest.hasColumn(column.getColumnName()));
        }
    }

    @Test
    public void shouldReturnNonForeignKeyColumnsInCorrectSort() {
        List<ColumnInfo> sortedNonForeignKeyColumns = new LinkedList<>();
        for (ColumnInfo column : tableUnderTest.getNonForeignKeyColumns()) {
            sortedNonForeignKeyColumns.add(column);
        }
        Collections.sort(sortedNonForeignKeyColumns);

        List<ColumnInfo> unsortedColumns = tableUnderTest.getNonForeignKeyColumns();
        for (int i = 0; i < sortedNonForeignKeyColumns.size(); i++) {
            assertEquals("Incorrect column sort", sortedNonForeignKeyColumns.get(i).getColumnName(), unsortedColumns.get(i).getColumnName());
        }
    }

    private List<ColumnInfo> createColumns(ColumnInfo[] nonDefaultColumns) {
        List<ColumnInfo> retList = new LinkedList<ColumnInfo>();
        for (ColumnInfo column : nonDefaultColumns) {
            retList.add(column);
        }
        return retList;
    }
}
