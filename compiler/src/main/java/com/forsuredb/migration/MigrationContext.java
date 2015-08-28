package com.forsuredb.migration;

import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.annotationprocessor.TableContext;
import com.forsuredb.annotationprocessor.TableInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MigrationContext implements TableContext {

    private final MigrationRetriever mr;
    private Map<String, TableInfo> tableMap;

    public MigrationContext(MigrationRetriever mr) {
        this.mr = mr;
    }

    @Override
    public boolean hasTable(String tableName) {
        createTableMapIfNull();
        return tableMap.containsKey(tableName);
    }

    @Override
    public TableInfo getTable(String tableName) {
        createTableMapIfNull();
        return tableMap.get(tableName);
    }

    @Override
    public Collection<TableInfo> allTables() {
        createTableMapIfNull();
        return tableMap.values();
    }

    private Map<String, TableInfo> createTables() {
        Map<String, TableInfo.Builder> tableBuilderMap = new HashMap<>();
        Map<String, ColumnInfo.Builder> columnBuilderMap = new HashMap<>();
        for (Migration m : mr.getMigrations()) {
            if (!m.isLastInSet()) { // <-- only process for migrations that are last in a set of migrations because the last in a set contains the extra information
                continue;
            }
            update(m, tableBuilderMap, columnBuilderMap);
        }

        for (Map.Entry<String, ColumnInfo.Builder> entry : columnBuilderMap.entrySet()) {
            TableInfo.Builder tb = tableBuilderMap.get(tableKeyFromColumnKey(entry.getKey()));
            tb.addColumn(entry.getValue().build());
        }

        Map<String, TableInfo> retMap = new HashMap<>();
        for (Map.Entry<String, TableInfo.Builder> entry : tableBuilderMap.entrySet()) {
            retMap.put(entry.getKey(), entry.getValue().build());
        }
        return retMap;

    }

    private void update(Migration m, Map<String, TableInfo.Builder> tableBuilderMap, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        switch (m.getMigrationType()) {
            case CREATE_TABLE:
                handleCreateTable(m, tableBuilderMap, columnBuilderMap);
                break;
            case ADD_FOREIGN_KEY_REFERENCE:
                handleAddForeignKeyReference(m, columnBuilderMap);
                break;
            case ALTER_TABLE_ADD_COLUMN:
                handleAddColumn(m, columnBuilderMap);
                break;
            case ALTER_TABLE_ADD_UNIQUE:
                handleAddUniqueColumn(m, columnBuilderMap);
        }
    }

    private void handleAddForeignKeyReference(Migration m, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        columnBuilderMap.put(columnKey(m), ColumnInfo.builder().columnName(m.getColumnName())
                .qualifiedType(m.getColumnQualifiedType())
                .foreignKey(true)
                .foreignKeyColumnName(m.getForeignKeyColumn())
                .foreignKeyTableName(m.getForeignKeyTable()));
    }

    private void handleAddUniqueColumn(Migration m, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        handleAddColumn(m, true, columnBuilderMap);
    }

    private void handleAddColumn(Migration m, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        handleAddColumn(m, false, columnBuilderMap);
    }

    private void handleAddColumn(Migration m, boolean unique, Map<String, ColumnInfo.Builder> columnBuilderMap) {
        ColumnInfo.Builder b = columnBuilderMap.get(columnKey(m));
        if (b == null) {
            b = ColumnInfo.builder().columnName(m.getColumnName()).qualifiedType(m.getColumnQualifiedType()).unique(unique);
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

    private void createTableMapIfNull() {
        if (tableMap == null) {
            tableMap = createTables();
        }
    }
}
