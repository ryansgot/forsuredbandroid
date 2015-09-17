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

import com.forsuredb.migration.MigrationContext;
import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.migration.sqlite.QueryGeneratorFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

// TODO: make this TableContext and MigrationContext agnostic.
/**
 * <p>
 *     Analyzes the diff between two {@link TableContext TableContext} objects--the one used to
 *     instantiate the DiffGenerator and the one passed to the
 *     {@link #analyzeDiff(TableContext) analyzeDiff(TableContext targetContext)} method.
 * </p>
 * @author Ryan Scott
 */
public class DiffGenerator {

    private static final String LOG_TAG = DiffGenerator.class.getSimpleName();

    private final TableContext context;

    public DiffGenerator(MigrationContext context) {
        this.context = context;
    }

    /**
     * <p>
     *     Anayzes the diff between this instance's {@link TableContext TableContext} and that of
     *     the argument
     * </p>
     * @param targetContext The {@link TableContext TableContext} that you would like to reach from
     *                      the {@link TableContext TableContext} member of this
     *                      {@link DiffGenerator DiffGenerator}
     * @return a priority queue of {@link QueryGenerator QueryGenerator} that generate a sequence of
     * queries that allow you to migrate a database from the schema of this instance's TableContext
     * to that of the {@link TableContext TableContext} argument.
     */
    public PriorityQueue<QueryGenerator> analyzeDiff(TableContext targetContext) {
        APLog.i(LOG_TAG, "analyzing diff: targetContext.allTables().size() = " + targetContext.allTables().size());
        PriorityQueue<QueryGenerator> retQueue = new PriorityQueue<>();
        for (TableInfo targetTable : targetContext.allTables()) {
            if (tableCreateQueryAppended(retQueue, targetTable)) {
                APLog.i(LOG_TAG, "Not checking column diffs for table: " + targetTable.getTableName());
                continue;
            }
            retQueue.addAll(getColumnChangeQueryGenerators(context.getTable(targetTable.getTableName()), targetTable));
        }

        return retQueue;
    }

    private boolean tableCreateQueryAppended(PriorityQueue<QueryGenerator> retQueue, TableInfo table) {
        APLog.i(LOG_TAG, "checking whether migration context has table: " + table.getTableName());
        if (context.hasTable(table.getTableName())) {
            APLog.i(LOG_TAG, table.getTableName() + " table PREVIOUSLY EXISTED. NOT creating a migration for it");
            return false;
        }

        APLog.i(LOG_TAG, table.getTableName() + " table did not previously exist. Creating migration for it");
        retQueue.add(QueryGeneratorFactory.createForTable(table));
        retQueue.addAll(getColumnChangeQueryGenerators(null, table));

        return true;
    }

    private Collection<? extends QueryGenerator> getColumnChangeQueryGenerators(TableInfo sourceTable, TableInfo targetTable) {
        List<QueryGenerator> retList = new LinkedList<>();
        for (ColumnInfo column : targetTable.getColumns()) {
            if (TableInfo.DEFAULT_COLUMNS.containsKey(column.getColumnName())) {
                continue;   // <-- columns in the default columns map are added when the table is created
            }
            if (sourceTable == null || !sourceTable.hasColumn(column.getColumnName())) {
                retList.add(QueryGeneratorFactory.createForColumn(targetTable, column));
            }
        }
        return retList;
    }
}
