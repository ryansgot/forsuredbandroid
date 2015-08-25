package com.forsuredb.migration.sqlite;

import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.annotationprocessor.TableInfo;
import com.forsuredb.migration.QueryGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CreateTempTableFromExisting extends QueryGenerator {

    private final TableInfo table;
    private final Map<String, ColumnInfo> excludedColumnsMap = new HashMap<>();

    public CreateTempTableFromExisting(TableInfo table, ColumnInfo... excludedColumns) {
        super(table.getTableName(), MigrationType.ADD_FOREIGN_KEY_REFERENCE);
        this.table = table;
        for (ColumnInfo column : excludedColumns) {
            this.excludedColumnsMap.put(column.getColumnName(), column);
        }
    }

    @Override
    public List<String> generate() {
        List<String> retList = new LinkedList<>();
        retList.addAll(new DropTableGenerator(tempTableName()).generate());
        retList.add(getCopyTableQuery());
        return retList;
    }

    private String getCopyTableQuery() {
        StringBuffer buf = new StringBuffer("CREATE TEMP TABLE ").append(tempTableName()).append(" AS SELECT ");
        List<ColumnInfo> columns = new LinkedList<>(table.getColumns());
        Collections.sort(columns);
        for (ColumnInfo column : columns) {
            if (excludedColumnsMap.containsKey(column.getColumnName())) {
                continue;
            }
            buf.append("_id".equals(column.getColumnName()) ? "" : ", ").append(column.getColumnName());
        }
        return buf.append(" FROM ").append(getTableName()).append(";").toString();
    }

    private String tempTableName() {
        return "temp_" + table.getTableName();
    }
}
