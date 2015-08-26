package com.forsuredb.migration.sqlite;

import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.migration.QueryGenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AddUniqueColumnGenerator extends QueryGenerator {


    private final ColumnInfo column;

    public AddUniqueColumnGenerator(String tableName, ColumnInfo column) {
        super(tableName, MigrationType.ALTER_TABLE_ADD_UNIQUE);
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
        List<String> retList = new LinkedList<>();
        retList.addAll(new AddColumnGenerator(getTableName(), column).generate());
        retList.add("CREATE UNIQUE INDEX " + column.getColumnName() + " ON " + getTableName() + "(" + column.getColumnName() + ");");
        return retList;
    }
}
