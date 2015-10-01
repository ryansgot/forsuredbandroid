/*
   forsuredb, an object relational mapping tool

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.forsuredb.migration.sqlite;

import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.annotationprocessor.TableInfo;
import com.forsuredb.migration.QueryGenerator;

public class QueryGeneratorFactory {

    public static QueryGenerator createForTable(TableInfo table) {
        return new CreateTableGenerator(table.getTableName());
    }

    public static QueryGenerator createForNewColumn(TableInfo table, ColumnInfo column) {
        if (column.isForeignKey()) {
            return new AddForeignKeyGenerator(table, column);
        }
        if (column.isUnique()) {
            return new AddUniqueColumnGenerator(table.getTableName(), column);
        }
        return new AddColumnGenerator(table.getTableName(), column);
    }

    public static QueryGenerator createForExistingColumn(String tableName, ColumnInfo existingColumn, ColumnInfo targetColumn) {
        if (targetColumn.isUnique() && !existingColumn.isUnique()) {
            return new AddUniqueIndexGenerator(tableName, targetColumn);
        }
        return null;
    }
}
