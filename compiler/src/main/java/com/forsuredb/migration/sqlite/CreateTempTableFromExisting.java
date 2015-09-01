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
