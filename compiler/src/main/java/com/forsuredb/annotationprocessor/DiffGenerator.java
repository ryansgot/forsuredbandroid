package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.MigrationContext;
import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.migration.sqlite.AddColumnGenerator;
import com.forsuredb.migration.sqlite.AddForeignKeyGenerator;
import com.forsuredb.migration.sqlite.CreateTableGenerator;

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
     * @param otherContext
     * @return
     */
    public PriorityQueue<QueryGenerator> analyzeDiff(TableContext otherContext) {
        PriorityQueue<QueryGenerator> retQueue = new PriorityQueue<>();
        for (TableInfo table : otherContext.allTables()) {
            if (tableCreateQueryAppended(retQueue, table)) {
                continue;
            }

            TableInfo mcTable = context.getTable(table.getTableName());
            for (ColumnInfo column : table.getNonForeignKeyColumns()) {
                if ("_id".equals(column.getColumnName())) {
                    continue;
                }
                if (!mcTable.hasColumn(column.getColumnName())) {
                    printMessage(Diagnostic.Kind.NOTE, table.getTableName() + "." + column.getColumnName() + " column did not previously exist. Creating migration for it");
                    retQueue.add(new AddColumnGenerator(table.getTableName(), column));
                }
            }
            for (ColumnInfo column : table.getForeignKeyColumns()) {
                if ("_id".equals(column.getColumnName())) {
                    continue;
                }
                if (!mcTable.hasColumn(column.getColumnName())) {
                    printMessage(Diagnostic.Kind.NOTE, table.getTableName() + "." + column.getColumnName() + " foreign key column did not previously exist. Creating foreign key migration for it");
                    retQueue.add(new AddForeignKeyGenerator(table, column));
                }
            }
        }

        return retQueue;
    }

    private boolean tableCreateQueryAppended(PriorityQueue<QueryGenerator> retQueue, TableInfo table) {
        if (context.hasTable(table.getTableName())) {
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
