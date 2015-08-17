package com.forsuredb.annotationprocessor;

import java.util.Collection;

public interface TableContext {
    boolean hasTable(String tableName);
    TableInfo getTable(String tableName);
    Collection<TableInfo> allTables();
}
