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
                        TestData.table().addColumn(TestData.longCol().foreignKey(true).foreignKeyColumnName("user_id").foreignKeyTableName("user").build()).build(),
                        new ColumnInfo[] {},
                        new String[] {
                                "DROP TABLE IF EXISTS temp_" + TestData.TABLE_NAME + ";",
                                "CREATE TEMP TABLE temp_" + TestData.TABLE_NAME + " AS SELECT _id, created, deleted, modified, long_column FROM " + TestData.TABLE_NAME + ";"
                        }
                },
                // Copy a table with an excluded column
                {
                        TestData.table().addColumn(TestData.longCol().foreignKey(true).foreignKeyColumnName("user_id").foreignKeyTableName("user").build()).build(),
                        new ColumnInfo[] {
                                TestData.longCol().foreignKey(true).foreignKeyColumnName("user_id").foreignKeyTableName("user").build()
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
