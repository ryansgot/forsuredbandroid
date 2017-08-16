package com.fsryan.forsuredb;

import com.fsryan.forsuredb.api.FSProjection;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ProjectionHelperTest {

    @Test
    public void shouldCorrectlyFormatSingleFSProjection() {
        final String[] expected = new String[] {
                "table_name.column1 AS table_name_column1",
                "table_name.column2 AS table_name_column2",
                "table_name.column3 AS table_name_column3"
        };
        assertArrayEquals(expected, ProjectionHelper.formatProjection(fsProjection("table_name", "column1", "column2", "column3")));
    }

    @Test
    public void shouldCorrectlyFormatSingleDistinctProjection() {
        final String[] expected = new String[] {
                "table_name.column1 AS table_name_column1",
                "table_name.column2 AS table_name_column2",
                "table_name.column3 AS table_name_column3"
        };
        assertArrayEquals(expected, ProjectionHelper.formatProjection(fsProjection("table_name", true, "column1", "column2", "column3")));
    }

    @Test
    public void shouldCorrectlyFormatTwoProjections() {
        final String[] expected = new String[] {
                "table_name.column1 AS table_name_column1",
                "table_name.column2 AS table_name_column2",
                "table_name.column3 AS table_name_column3",
                "table2_name.column1 AS table2_name_column1"
        };
        assertArrayEquals(expected, ProjectionHelper.formatProjection(
                fsProjection("table_name", "column1", "column2", "column3"),
                fsProjection("table2_name", "column1")
        ));
    }

    @Test
    public void shouldReturnNullWhenOnlyFSProjectionIsNull() {
        assertArrayEquals(null, ProjectionHelper.formatProjection(null));
    }

    private static FSProjection fsProjection(String tableName, String... columns) {
        return fsProjection(tableName, false, columns);
    }

    private static FSProjection fsProjection(final String tableName, final boolean distinct, final String... columns) {
        return new FSProjection() {
            @Override
            public String tableName() {
                return tableName;
            }

            @Override
            public String[] columns() {
                return columns;
            }

            @Override
            public boolean isDistinct() {
                return distinct;
            }
        };
    }
}
