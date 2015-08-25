package com.forsuredb.migration.sqlite;

import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.annotationprocessor.TableInfo;
import com.forsuredb.migration.QueryGenerator;

public class QueryGeneratorFactory {

    public static QueryGenerator createForTable(TableInfo table) {
        return new CreateTableGenerator(table.getTableName());
    }

    public static QueryGenerator createForColumn(TableInfo table, ColumnInfo column) {
        if (column.isForeignKey()) {
            return new AddForeignKeyGenerator(table, column);
        }
        if (column.isUnique()) {
            return new AddUniqueColumnGenerator(table.getTableName(), column);
        }
        return new AddColumnGenerator(table.getTableName(), column);
    }
}
