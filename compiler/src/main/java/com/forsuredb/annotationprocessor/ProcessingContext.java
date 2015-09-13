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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * <p>
 *     This is the TableContext that corresponds to the currently defined extensions of the
 *     {@link FSGetApi FSGetApi} interface annotated with the {@link FSTable FSTable} annotation.
 * </p>
 * @author Ryan Scott
 */
public class ProcessingContext implements TableContext {

    private final Set<TypeElement> tableTypes = new HashSet<>();
    private final ProcessingEnvironment processingEnv;
    private Map<String, TableInfo> tableMap;

    public ProcessingContext(Set<TypeElement> tableTypes, ProcessingEnvironment processingEnv) {
        this.tableTypes.addAll(tableTypes);
        this.processingEnv = processingEnv;
    }

    @Override
    public Collection<TableInfo> allTables() {
        createTableMapIfNecessary();
        return tableMap.values();
    }

    @Override
    public boolean hasTable(String tableName) {
        createTableMapIfNecessary();
        return tableName == null ? false : tableMap.containsKey(tableName);
    }

    @Override
    public TableInfo getTable(String tableName) {
        createTableMapIfNecessary();
        return tableName == null ? null : tableMap.get(tableName);
    }

    private void createTableMapIfNecessary() {
        if (tableMap != null) {
            return;
        }

        tableMap = new HashMap<String, TableInfo>();
        List<TableInfo> allTables = gatherInitialInfo();
        for (TableInfo table : allTables) {
            for (ColumnInfo column : table.getColumns()) {
                column.enrichWithForeignTableInfo(allTables);
            }
            tableMap.put(table.getTableName(), table);
            if (processingEnv != null) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, table.toString());
            }
        }
    }

    private List<TableInfo> gatherInitialInfo() {
        List<TableInfo> ret = new ArrayList<>();
        for (TypeElement te : tableTypes) {
            if (te.getKind() != ElementKind.INTERFACE) {
                continue;   // <-- only process interfaces
            }

            ret.add(TableInfo.from(te));
        }

        return ret;
    }
}
