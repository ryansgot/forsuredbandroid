package com.forsuredb.migration.sqlite;

import com.forsuredb.TestData;
import com.forsuredb.migration.QueryGenerator;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class QueryGeneratorFactoryTest {

    @Test
    public void shouldCreateCreateTableQueryGeneratorInstance() {
        QueryGenerator qg = QueryGeneratorFactory.createForTable(TestData.table().build());
        assertTrue("createForTable method did not return an instance of the CreateTableQueryGenerator class", qg instanceof CreateTableGenerator);
    }

    @Test
    public void shouldCreateAddColumnGeneratorForNonUniqueColumn() {
        QueryGenerator qg = QueryGeneratorFactory.createForColumn(TestData.table().build(), TestData.intCol().build());
        assertTrue("When column was nonUnique, the returned query generator was not an instance of AddColumnGenerator", qg instanceof AddColumnGenerator);
    }

    @Test
    public void shouldCreateAddUniqueColumnGeneratorForUniqueColumn() {
        QueryGenerator qg = QueryGeneratorFactory.createForColumn(TestData.table().build(), TestData.intCol().unique(true).build());
        assertTrue("When column was unique, the returned query generator was not an instance of AddUniqueColumnGenerator", qg instanceof AddUniqueColumnGenerator);
    }

    @Test
    public void shouldCreateAddColumnGeneratorForForeignKeyColumn() {
        QueryGenerator qg = QueryGeneratorFactory.createForColumn(TestData.table().build(), TestData.intCol().foreignKey(true).build());
        assertTrue("When column was foreign key, the returned query generator was not an instance of AddForeignKeyGenerator", qg instanceof AddForeignKeyGenerator);
    }
}
