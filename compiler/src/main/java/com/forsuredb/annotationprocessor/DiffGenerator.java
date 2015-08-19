package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.MigrationContext;
import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.migration.sqlite.AddColumnGenerator;
import com.forsuredb.migration.sqlite.AddForeignKeyGenerator;
import com.forsuredb.migration.sqlite.CreateTableGenerator;

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
            retQueue.addAll(getColumnChangeQueryGenerators(targetTable));
        }

        return retQueue;
    }

    private Collection<? extends QueryGenerator> getColumnChangeQueryGenerators(TableInfo targetTable) {
        TableInfo sourceTable = context.getTable(targetTable.getTableName());
        List<QueryGenerator> retList = new LinkedList<>();
        for (ColumnInfo column : targetTable.getNonForeignKeyColumns()) {
            if ("_id".equals(column.getColumnName())) {
                continue;
            }
            if (!sourceTable.hasColumn(column.getColumnName())) {
                printMessage(Diagnostic.Kind.NOTE, sourceTable.getTableName() + "." + column.getColumnName() + " column did not previously exist. Creating migration for it");
                retList.add(new AddColumnGenerator(sourceTable.getTableName(), column));
            } else {
                printMessage(Diagnostic.Kind.NOTE, sourceTable.getTableName() + "." + column.getColumnName() + " column PREVIOUSLY EXISTED. NOT creating migration for it");
            }
        }
        for (ColumnInfo column : targetTable.getForeignKeyColumns()) {
            if ("_id".equals(column.getColumnName())) {
                continue;
            }
            if (!sourceTable.hasColumn(column.getColumnName())) {
                printMessage(Diagnostic.Kind.NOTE, sourceTable.getTableName() + "." + column.getColumnName() + " foreign key column did not previously exist. Creating foreign key migration for it");
                retList.add(new AddForeignKeyGenerator(sourceTable, column));
            } else {
                printMessage(Diagnostic.Kind.NOTE, sourceTable.getTableName() + "." + column.getColumnName() + " foreign key column PREVIOUSLY EXISTED. NOT creating migration for it");
            }
        }

        return retList;
    }

    private boolean tableCreateQueryAppended(PriorityQueue<QueryGenerator> retQueue, TableInfo table) {
        printMessage(Diagnostic.Kind.NOTE, "checking whether migration context has table: " + table.getTableName());
        if (context.hasTable(table.getTableName())) {
            printMessage(Diagnostic.Kind.NOTE, table.getTableName() + " table PREVIOUSLY EXISTED. NOT creating a migration for it");
            return false;
        }

        printMessage(Diagnostic.Kind.NOTE, table.getTableName() + " table did not previously exist. Creating migration for it");
        retQueue.add(new CreateTableGenerator(table.getTableName()));
        for (ColumnInfo column : table.getForeignKeyColumns()) {
            if ("_id".equals(column.getColumnName())) {
                continue;
            }
            retQueue.add(new AddForeignKeyGenerator(table, column));
        }
        for (ColumnInfo column : table.getNonForeignKeyColumns()) {
            if ("_id".equals(column.getColumnName())) {
                continue;
            }
            retQueue.add(new AddColumnGenerator(table.getTableName(), column));
        }

        return true;
    }

    private void printMessage(Diagnostic.Kind kind, String message) {
        if (processingEnv != null) {
            processingEnv.getMessager().printMessage(kind, message);
        }
    }
}
