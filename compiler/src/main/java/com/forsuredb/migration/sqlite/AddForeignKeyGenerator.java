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

import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.annotationprocessor.TableInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AddForeignKeyGenerator extends QueryGenerator {

    private final TableInfo table;
    private final ColumnInfo column;

    public AddForeignKeyGenerator(TableInfo table, ColumnInfo column) {
        super(table.getTableName(), MigrationType.ADD_FOREIGN_KEY_REFERENCE);
        this.table = table;
        this.column = column;
    }

    @Override
    public List<String> generate() {
        List<String> retList = new LinkedList<>();

        retList.addAll(new CreateTempTableFromExisting(table, column).generate());
        retList.addAll(new DropTableGenerator(getTableName()).generate());
        retList.addAll(recreateTableWithAllForeignKeysQuery());
        retList.addAll(allColumnAdditionQueries());
        retList.add(reinsertDataQuery());
        retList.addAll(new DropTableGenerator(tempTableName()).generate());

        return retList;
    }

    @Override
    public Map<String, String> getAdditionalAttributes() {
        Map<String, String> ret = new HashMap<>();
        ret.put("column", column.getColumnName());
        ret.put("column_type", column.getQualifiedType());
        ret.put("foreign_key_table", column.getForeignKeyTableName());
        ret.put("foreign_key_column", column.getForeignKeyColumnName());
        return ret;
    }

    private List<String> recreateTableWithAllForeignKeysQuery() {
        final List<String> retList = new LinkedList<>();
        List<String> normalCreationQueries = new CreateTableGenerator(getTableName()).generate();

        // add the default columns to the normal TABLE CREATE query
        StringBuffer buf = new StringBuffer(normalCreationQueries.remove(0));
        buf.delete(buf.length() - 2, buf.length());   // <-- removes );
        List<ColumnInfo> foreignKeyColumns = table.getForeignKeyColumns();
        addColumnDefinitionsToBuffer(buf, foreignKeyColumns);
        addColumnDefinitionToBuffer(buf, column);
        addForeignKeyDefinitionsToBuffer(buf, foreignKeyColumns);
        addForeignKeyDefinitionToBuffer(buf, column);
        retList.add(buf.append(");").toString());

        // add all remaining table create queries
        while (normalCreationQueries.size() > 0) {
            retList.add(normalCreationQueries.remove(0));
        }

        return retList;
    }

    private List<String> allColumnAdditionQueries() {
        List<String> retList = new LinkedList<>();
        for (ColumnInfo columnInfo : table.getNonForeignKeyColumns()) {
            if (TableInfo.DEFAULT_COLUMNS.containsKey(columnInfo.getColumnName())) {
                continue;   // <-- these columns were added in the CREATE TABLE query
            }

            retList.addAll(new AddColumnGenerator(getTableName(), columnInfo).generate());
        }

        return retList;
    }

    private String reinsertDataQuery() {
        StringBuffer buf = new StringBuffer("INSERT INTO ").append(getTableName()).append(" SELECT ");
        List<ColumnInfo> tableColumns = new LinkedList<>(table.getColumns());
        Collections.sort(tableColumns);
        // append all of the previously existing columns first
        for (ColumnInfo tableColumn : tableColumns) {
            if (tableColumn.getColumnName().equals(column.getColumnName())) {
                buf.append(", null AS ").append(column.getColumnName());
            } else {
                buf.append("_id".equals(tableColumn.getColumnName()) ? "" : ", ").append(tableColumn.getColumnName());
            }
        }
        // append the new foreign key last
        return buf.append(" FROM ").append(tempTableName()).append(";").toString();
    }

    private String tempTableName() {
        return "temp_" + getTableName();
    }

    private void addColumnDefinitionsToBuffer(StringBuffer buf, List<ColumnInfo> columns) {
        for (ColumnInfo column : columns) {
            if (!column.getColumnName().equals(this.column.getColumnName())) {
                addColumnDefinitionToBuffer(buf, column);
            }
        }
    }

    private void addColumnDefinitionToBuffer(StringBuffer buf, ColumnInfo column) {
        buf.append(", ").append(column.getColumnName())
                .append(" ").append(TypeTranslator.from(column.getQualifiedType()).getSqlString());
    }

    private void addForeignKeyDefinitionsToBuffer(StringBuffer buf, List<ColumnInfo> foreignKeyColumns) {
        for (ColumnInfo foreignKeyColumn : foreignKeyColumns) {
            if (!foreignKeyColumn.getColumnName().equals(column.getColumnName())) {
                addForeignKeyDefinitionToBuffer(buf, foreignKeyColumn);
            }
        }
    }

    private void addForeignKeyDefinitionToBuffer(StringBuffer buf, ColumnInfo foreignKeyColumn) {
        buf.append(", FOREIGN KEY(").append(foreignKeyColumn.getColumnName())
                .append(") REFERENCES ").append(foreignKeyColumn.getForeignKeyTableName())
                .append("(").append(foreignKeyColumn.getForeignKeyColumnName())
                .append(")");
    }
}
