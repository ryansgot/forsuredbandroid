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
