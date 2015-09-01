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
