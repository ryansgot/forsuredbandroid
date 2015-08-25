package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.MigrationContext;
import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.migration.sqlite.QueryGeneratorFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;

public class DiffGenerator {

    private final TableContext context;
    private final ProcessingEnvironment processingEnv;

    public DiffGenerator(MigrationContext context) {
        this(context, null);
    }

    public DiffGenerator(MigrationContext context, ProcessingEnvironment processingEnv) {
        this.context = context;
        this.processingEnv = processingEnv;
    }

    /**
     * <p>
     *     Anayzes the diff between this instance' TableContext and the table context passed in and
     *     delivers a priority queue of QueryGenerators that generate a sequence of queries that
     *     allow you to migrate a database from the schema of this instance's TableContext to that
     *     of the argument.
     * </p>
     * @param targetContext
     * @return
     */
    public PriorityQueue<QueryGenerator> analyzeDiff(TableContext targetContext) {
        printMessage(Diagnostic.Kind.NOTE, "analyzing diff: targetContext.allTables().size() = " + targetContext.allTables().size());
        PriorityQueue<QueryGenerator> retQueue = new PriorityQueue<>();
        for (TableInfo targetTable : targetContext.allTables()) {
            if (tableCreateQueryAppended(retQueue, targetTable)) {
                printMessage(Diagnostic.Kind.NOTE, "Not checking column diffs for table: " + targetTable.getTableName());
                continue;
            }
            retQueue.addAll(getColumnChangeQueryGenerators(context.getTable(targetTable.getTableName()), targetTable));
        }

        return retQueue;
    }

    private boolean tableCreateQueryAppended(PriorityQueue<QueryGenerator> retQueue, TableInfo table) {
        printMessage(Diagnostic.Kind.NOTE, "checking whether migration context has table: " + table.getTableName());
        if (context.hasTable(table.getTableName())) {
            printMessage(Diagnostic.Kind.NOTE, table.getTableName() + " table PREVIOUSLY EXISTED. NOT creating a migration for it");
            return false;
        }

        printMessage(Diagnostic.Kind.NOTE, table.getTableName() + " table did not previously exist. Creating migration for it");
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

    private void printMessage(Diagnostic.Kind kind, String message) {
        if (processingEnv != null) {
            processingEnv.getMessager().printMessage(kind, message);
        }
    }
}
