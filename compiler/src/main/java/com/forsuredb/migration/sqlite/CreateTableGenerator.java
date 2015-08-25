package com.forsuredb.migration.sqlite;

import com.forsuredb.migration.QueryGenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CreateTableGenerator extends QueryGenerator {

    public CreateTableGenerator(String tableName) {
        super(tableName,MigrationType.CREATE_TABLE);
    }

    @Override
    public Map<String, String> getAdditionalAttributes() {
        Map<String, String> ret = new HashMap<>();
        ret.put("column", "_id");
        ret.put("column_type", "long");
        return ret;
    }

    @Override
    public List<String> generate() {
        List<String> queries = new LinkedList<>();
        queries.add(createTableQuery());
        queries.add(modifiedTriggerQuery());
        return queries;
    }

    private String createTableQuery() {
        return new StringBuffer("CREATE TABLE ").append(getTableName())
                .append("(_id INTEGER PRIMARY KEY")
                .append(", created DATETIME DEFAULT CURRENT_TIMESTAMP")
                .append(", deleted INTEGER DEFAULT 0")
                .append(", modified DATETIME DEFAULT CURRENT_TIMESTAMP);")
                .toString();
    }

    private String modifiedTriggerQuery() {
        return new StringBuffer("CREATE TRIGGER ").append(getTableName())
                .append("_updated_trigger AFTER UPDATE ON ")
                .append(getTableName())
                .append(" BEGIN UPDATE ")
                .append(getTableName())
                .append(" SET modified=CURRENT_TIMESTAMP WHERE _id=NEW._id; END;")
                .toString();
    }
}
