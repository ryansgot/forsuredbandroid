package com.forsuredb.migration.sqlite;

import com.forsuredb.migration.QueryGenerator;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class BaseSQLiteGeneratorTest {

    private List<String> expectedSql;

    public BaseSQLiteGeneratorTest(String[] expectedSql) {
        this.expectedSql = new LinkedList<>();
        for (String sql : expectedSql) {
            this.expectedSql.add(sql);
        }
    }

    @Test
    public void shouldHaveCorrectNumberOfQueries() {
        assertEquals(expectedSql.size(), getGenerator().generate().size());
    }

    @Test
    public void shouldMatchExpectedQueries() {
        List<String> generatedSql = getGenerator().generate();
        for (int i = 0; i < expectedSql.size(); i++) {
            assertEquals(expectedSql.get(i), generatedSql.get(i));
        }
    }

    @Test
    public void allQueriesEndInSemicolon() {
        for (String sql : getGenerator().generate()) {
            assertTrue("Statement did not end in ';' " + sql, sql.endsWith(";"));
        }
    }

    protected abstract QueryGenerator getGenerator();
}
