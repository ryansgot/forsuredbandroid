package com.forsuredb.migration.sqlite;

import com.forsuredb.TestData;
import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.migration.QueryGenerator;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class AddUniqueColumnGeneratorTest extends BaseSQLiteGeneratorTest {

    private AddUniqueColumnGenerator generatorUnderTest;

    private ColumnInfo column;

    public AddUniqueColumnGeneratorTest(ColumnInfo column, String... expectedSql) {
        super(expectedSql);
        this.column = column;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        TestData.stringCol().build(),
                        new String[] {
                                "ALTER TABLE " + TestData.TABLE_NAME + " ADD COLUMN string_column TEXT;",
                                "CREATE UNIQUE INDEX string_column ON " + TestData.TABLE_NAME + "(string_column);"
                        }
                },
        });
    }

    @Before
    public void setUp() {
        generatorUnderTest = new AddUniqueColumnGenerator(TestData.TABLE_NAME, column);
    }

    @Override
    protected QueryGenerator getGenerator() {
        return generatorUnderTest;
    }
}
