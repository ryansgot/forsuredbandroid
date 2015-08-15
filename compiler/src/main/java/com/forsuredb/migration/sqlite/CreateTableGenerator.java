package com.forsuredb.migration.sqlite;

import com.forsuredb.migration.QueryGenerator;

import java.util.LinkedList;
import java.util.List;

public class CreateTableGenerator extends QueryGenerator {

    public CreateTableGenerator(String tableName) {
        super(tableName,MigrationType.CREATE_TABLE);
    }

    @Override
    public List<String> generate() {
        List<String> queries = new LinkedList<>();
        queries.add(new StringBuffer("CREATE TABLE ").append(getTableName())
                                                     .append("(_id INTEGER PRIMARY KEY);")
                                                     .toString());
        return queries;
    }
}
