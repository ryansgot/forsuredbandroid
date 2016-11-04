package com.fsryan.forsuredb;

import com.fsryan.forsuredb.sqlitelib.SqlGenerator;

/**
 * <p>
 *     Whereas the stock {@link SqlGenerator} does not make any framework assumptions, this class does.
 *     It accounts for usage of {@link android.database.sqlite.SQLiteCursor}, working around bug 903852,
 *     allowing more than one column of a join query to have the same name by providing a proper alias.
 * </p>
 */
public class FSAndroidSQLiteGenerator extends SqlGenerator {

    @Override
    public String unambiguousRetrievalColumn(String tableName, String columnName) {
        return tableName + "_" + columnName;
    }
}
