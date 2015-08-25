package com.forsuredb.annotationprocessor;

import com.forsuredb.TestData;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ColumnInfoTest {

    @Test
    public void shouldPutIdColumnFirst() {
        List<ColumnInfo> defaultColumns = new LinkedList<>();
        for (ColumnInfo column : TableInfo.DEFAULT_COLUMNS.values()) {
            defaultColumns.add(column);
        }
        Collections.sort(defaultColumns);
        assertEquals("_id", defaultColumns.get(0).getColumnName());
    }

    @Test
    public void shouldPutDefaultColumnsBeforeOthers() {
        ColumnInfo defaultColumn = TableInfo.DEFAULT_COLUMNS.get("created");
        assertTrue(defaultColumn.compareTo(TestData.longCol().build()) < 0);
    }

    @Test
    public void shouldPutDefaultColumnsBeforeForeignKeyColumns() {
        ColumnInfo defaultColumn = TableInfo.DEFAULT_COLUMNS.get("created");
        assertTrue(defaultColumn.compareTo(TestData.longCol().foreignKey(true).build()) < 0);
    }

    @Test
    public void shouldAlphabetizeSortDefaultColumns() {
        assertTrue(TableInfo.DEFAULT_COLUMNS.get("created").compareTo(TableInfo.DEFAULT_COLUMNS.get("modified")) < 0);
    }

    @Test
    public void shouldSortForeignKeyBeforeNonForeignKey() {
        ColumnInfo foreignKeyColumn = TestData.longCol().foreignKey(true).build();
        assertTrue("Did not sort foreign key column before non-foreign key", foreignKeyColumn.compareTo(TestData.longCol().build()) < 0);
    }

    @Test
    public void shouldSortNonForeignKeyAfterForeignKey() {
        ColumnInfo nonForeignKeyColumn = TestData.intCol().build();
        assertTrue("Did not sort non foreign key column after foreign key", nonForeignKeyColumn.compareTo(TestData.longCol().foreignKey(true).build()) > 0);
    }

    @Test
    public void shouldSortIdColumnBeforeOtherColumn() {
        assertTrue("Did not sort _id column before other column", TestData.idCol().compareTo(TestData.longCol().build()) < 0);
    }

    @Test
    public void shouldSortIdColumnBeforeForeignKey() {
        assertTrue("Did not sort _id column before foreign key", TestData.idCol().compareTo(TestData.longCol().foreignKey(true).build()) < 0);
    }

    @Test
    public void shouldAlphabetizeColumns() {
        assertTrue("Did not sort int_column before long_column", TestData.intCol().build().compareTo(TestData.longCol().build()) < 0);
    }

    @Test
    public void shouldSayHasDefaultValueAfterSettingDefaultValue() {
        assertTrue(TestData.booleanCol().defaultValue("0").build().hasDefaultValue());
    }
}
