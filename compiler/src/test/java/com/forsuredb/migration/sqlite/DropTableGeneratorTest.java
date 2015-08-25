package com.forsuredb.migration.sqlite;

import com.forsuredb.TestData;
import com.forsuredb.migration.QueryGenerator;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class DropTableGeneratorTest extends BaseSQLiteGeneratorTest {

    private DropTableGenerator generatorUnderTest;

    public DropTableGeneratorTest(String... expectedSql) {
        super(expectedSql);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {
                        new String[]{
                                "DROP TABLE IF EXISTS " + TestData.TABLE_NAME + ";"
                        }
                }
        });
    }

    @Override
    protected QueryGenerator getGenerator() {
        return generatorUnderTest;
    }

    @Before
    public void setUp() {
        generatorUnderTest = new DropTableGenerator(TestData.TABLE_NAME);
    }
}
