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
package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSTable;
import com.forsuredb.api.FSGetApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Modifier;

/**
 * <p>
 *     This is the TableContext that corresponds to the currently defined extensions of the
 *     {@link FSGetApi FSGetApi} interface annotated with the {@link FSTable FSTable} annotation.
 * </p>
 * @author Ryan Scott
 */
public class ProcessingContext implements TableContext {

    private static final String LOG_TAG = ProcessingContext.class.getSimpleName();

    private final Set<TypeElement> tableTypes = new HashSet<>();
    private Map<String, TableInfo> tableMap;
    private List<JoinInfo> joins;

    public ProcessingContext(Set<TypeElement> tableTypes) {
        this.tableTypes.addAll(tableTypes);
    }

    @Override
    public Collection<TableInfo> allTables() {
        createTableMapIfNecessary();
        return tableMap.values();
    }

    @Override
    public boolean hasTable(String tableName) {
        createTableMapIfNecessary();
        return tableName != null && tableMap.containsKey(tableName);
    }

    @Override
    public TableInfo getTable(String tableName) {
        createTableMapIfNecessary();
        return tableName == null ? null : tableMap.get(tableName);
    }

    //TODO: @Override
    public List<JoinInfo> allJoins() {
        createTableMapIfNecessary();
        return joins;
    }

    private void createTableMapIfNecessary() {
        if (tableMap != null) {
            return;
        }
        joins = new LinkedList<>();

        tableMap = new HashMap<String, TableInfo>();
        List<TableInfo> allTables = gatherInitialInfo();
        for (TableInfo table : allTables) {
            for (ColumnInfo column : table.getColumns()) {
                column.enrichWithForeignTableInfo(allTables);
            }
            tableMap.put(table.getTableName(), table);
            APLog.i(LOG_TAG, "created table: " + table.toString());
        }
        createJoinInfo();
    }

    private void createJoinInfo() {
        for (TableInfo table : tableMap.values()) {
            for (ColumnInfo column : table.getForeignKeyColumns()) {
                addToJoins(table, column);
            }
        }
    }

    private void addToJoins(TableInfo childTable, ColumnInfo childColumn) {
        TableInfo parent = tableMap.get(childColumn.getForeignKey().getTableName());
        JoinInfo join = JoinInfo.builder().childTable(childTable)
                .childColumn(childColumn)
                .parentTable(parent)
                .parentColumn(parent.getColumn(childColumn.getForeignKey().getColumnName()))
                .build();
        joins.add(join);
        APLog.i(LOG_TAG, "found join: " + join.toString());
    }

    private List<TableInfo> gatherInitialInfo() {
        List<TableInfo> ret = new ArrayList<>();
        for (TypeElement te : tableTypes) {
            if (!isNonPrivateInterface(te)) {
                continue;   // <-- only process interfaces that are non-private
            }

            ret.add(TableInfo.from(te));
        }

        return ret;
    }

    private boolean isNonPrivateInterface(TypeElement te) {
        return te.getKind() == ElementKind.INTERFACE && !te.getModifiers().contains(Modifier.PRIVATE);
    }
}
