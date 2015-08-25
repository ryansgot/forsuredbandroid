package com.forsuredb.migration.sqlite;

import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.annotationprocessor.ColumnInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AddColumnGenerator extends QueryGenerator {

    private final ColumnInfo column;

    public AddColumnGenerator(String tableName, ColumnInfo column) {
        super(tableName, MigrationType.ALTER_TABLE_ADD_COLUMN);
        this.column = column;
    }

    @Override
    public Map<String, String> getAdditionalAttributes() {
        Map<String, String> ret = new HashMap<>();
        ret.put("column", column.getColumnName());
        ret.put("column_type", column.getQualifiedType());
        return ret;
    }

    @Override
    public List<String> generate() {
        List<String> queries = new LinkedList<>();
        queries.add(new StringBuffer("ALTER TABLE ").append(getTableName())
                                                    .append(" ADD COLUMN ")
                                                    .append(column.getColumnName())
                                                    .append(" ")
                                                    .append(TypeTranslator.from(column.getQualifiedType()).getSqlString())
                                                    .append(column.hasDefaultValue() ? " DEFAULT " + column.getDefaultValue() : "")
                                                    .append(";")
                                                    .toString());
        return queries;
    }
}
