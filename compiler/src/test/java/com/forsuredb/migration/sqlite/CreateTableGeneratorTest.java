package com.forsuredb.migration.sqlite;

import com.forsuredb.TestData;
import com.forsuredb.migration.QueryGenerator;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
public class CreateTableGeneratorTest extends BaseSQLiteGeneratorTest {

    private CreateTableGenerator generatorUnderTest;

    public CreateTableGeneratorTest(String... expectedSql) {
        super(expectedSql);
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {
                        new String[] {
                                "CREATE TABLE " + TestData.TABLE_NAME + "(_id INTEGER PRIMARY KEY, created DATETIME DEFAULT CURRENT_TIMESTAMP, deleted INTEGER DEFAULT 0, modified DATETIME DEFAULT CURRENT_TIMESTAMP);",
                                "CREATE TRIGGER " + TestData.TABLE_NAME + "_updated_trigger AFTER UPDATE ON " + TestData.TABLE_NAME + " BEGIN UPDATE " + TestData.TABLE_NAME + " SET modified=CURRENT_TIMESTAMP WHERE _id=NEW._id; END;"
                        }
                }
        });
    }

    @Before
    public void setUp() {
        generatorUnderTest = new CreateTableGenerator(TestData.TABLE_NAME);
    }

    @Override
    protected QueryGenerator getGenerator() {
        return generatorUnderTest;
    }
}
