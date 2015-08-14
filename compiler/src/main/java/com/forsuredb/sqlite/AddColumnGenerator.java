package com.forsuredb.sqlite;

import com.forsuredb.QueryGenerator;
import com.forsuredb.annotationprocessor.ColumnInfo;

import java.util.LinkedList;
import java.util.List;

public class AddColumnGenerator extends QueryGenerator {

    private final ColumnInfo column;

    public AddColumnGenerator(String tableName, ColumnInfo column) {
        super(tableName, MigrationType.ALTER_TABLE_ADD_COLUMN);
        this.column = column;
    }

    @Override
    public List<String> generate() {
        List<String> queries = new LinkedList<>();
        queries.add(new StringBuffer("ALTER TABLE ").append(getTableName())
                                                    .append(" ADD COLUMN ")
                                                    .append(column.getColumnName())
                                                    .append(" ")
                                                    .append(TypeTranslator.from(column.getType()).getSqlString())
                                                    .append(";")
                                                    .toString());
        return queries;
    }
}
