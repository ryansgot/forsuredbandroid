package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.Migration;
import com.forsuredb.migration.MigrationRetriever;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MigrationContext {

    private final MigrationRetriever mr;
    private List<TableInfo> allTables;

    public MigrationContext(MigrationRetriever mr) {
        this.mr = mr;
    }

    public List<TableInfo> allTables() {
        if (allTables == null) {
            allTables = createTables();
        }
        return allTables;
    }

    private List<TableInfo> createTables() {
        Map<String, TableInfo.Builder> tableBuilderMap = new HashMap<>();
        Map<String, ColumnInfo.Builder> columnBuilderMap = new HashMap<>();
        for (Migration m : mr.orderedMigrations()) {
            if (!m.isLastInSet()) { // <-- only process for migrations that are last in a set of migrations because the last in a set contains the extra information
                continue;
            }
            update(m, tableBuilderMap, columnBuilderMap);
        }

        for (Map.Entry<String, ColumnInfo.Builder> entry : columnBuilderMap.entrySet()) {
            TableInfo.Builder tb = tableBuilderMap.get(tableKeyFromColumnKey(entry.getKey()));
            tb.addColumn(entry.getValue().build());
        }
        List<TableInfo> retList = new LinkedList<>();
        for (TableInfo.Builder tb : tableBuilderMap.values()) {
            TableInfo table = tb.build();
            retList.add(table);
        }
        return retList;

    }

    private void update(Migration m, Map<String, TableInfo.Builder> tableBuilderMap, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        switch (m.getMigrationType()) {
            case CREATE_TABLE:
                handleCreateTable(m, tableBuilderMap, columnBuilderMap);
                break;
            case ALTER_TABLE_ADD_COLUMN:
                handleAddColumn(m, columnBuilderMap);
                break;
            case ADD_FOREIGN_KEY_REFERENCE:
                handleAddForeignKeyReference(m, columnBuilderMap);
                break;
        }
    }

    private void handleAddForeignKeyReference(Migration m, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        columnBuilderMap.put(columnKey(m), ColumnInfo.builder().columnName(m.getColumnName())
                .qualifiedType(m.getColumnQualifiedType())
                .foreignKey(true)
                .foreignKeyColumnName(m.getForeignKeyColumn())
                .foreignKeyTableName(m.getForeignKeyTable()));
    }

    private void handleAddColumn(Migration m, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        ColumnInfo.Builder b = columnBuilderMap.get(columnKey(m));
        if (b == null) {
            b = ColumnInfo.builder().columnName(m.getColumnName()).qualifiedType(m.getColumnQualifiedType());
            columnBuilderMap.put(columnKey(m), b);
        }
    }

    private void handleCreateTable(Migration m, Map<String, TableInfo.Builder> tableBuilderMap, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        TableInfo.Builder tb = tableBuilderMap.get(tableKey(m));
        if (tb == null) {
            tb = TableInfo.builder().tableName(m.getTableName());
            tableBuilderMap.put(tableKey(m), tb);
        }
        ColumnInfo.Builder cb = columnBuilderMap.get(columnKey(m));
        if (cb == null) {
            cb = ColumnInfo.builder().columnName(m.getColumnName()).qualifiedType(m.getColumnQualifiedType()).primaryKey(true);
            columnBuilderMap.put(columnKey(m), cb);
        }

    }

    private String tableKey(Migration m) {
        return m.getTableName();
    }

    private String columnKey(Migration m) {
        return tableKey(m) + "." + m.getColumnName();
    }

    private String tableKeyFromColumnKey(String columnBuilderMapKey) {
        return columnBuilderMapKey.split("\\.")[0];
    }
}
