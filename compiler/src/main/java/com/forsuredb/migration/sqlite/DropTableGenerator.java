package com.forsuredb.migration.sqlite;

import com.forsuredb.migration.QueryGenerator;

import java.util.LinkedList;
import java.util.List;

public class DropTableGenerator extends QueryGenerator {

    public DropTableGenerator(String tableName) {
        super(tableName, MigrationType.DROP_TABLE);
    }

    @Override
    public List<String> generate() {
        List<String> retList = new LinkedList<>();
        retList.add("DROP TABLE IF EXISTS " + getTableName() + ";");
        return retList;
    }
}
