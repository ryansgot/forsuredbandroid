package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.Migration;
import com.forsuredb.migration.MigrationContext;
import com.forsuredb.migration.MigrationRetriever;
import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.migration.sqlite.AddColumnGenerator;
import com.forsuredb.migration.sqlite.AddForeignKeyGenerator;
import com.forsuredb.migration.sqlite.CreateTableGenerator;

import org.apache.velocity.VelocityContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/*package*/ class MigrationGenerator extends BaseGenerator<FileObject> {

    private final Date date;
    private final List<TableInfo> allTables;
    private final MigrationRetriever mr;

    public MigrationGenerator(List<TableInfo> allTables, String migrationDirectory, ProcessingEnvironment processingEnv)  {
        super(processingEnv);
        date = new Date();
        this.allTables = allTables;
        mr = new MigrationRetriever(new MigrationFileRetriever(migrationDirectory));
    }

    @Override
    protected FileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return new ResourceCreator(getRelativeFileName()).create(processingEnv);
    }

    @Override
    protected VelocityContext createVelocityContext() {
        PriorityQueue<QueryGenerator> queryGenerators = analyzeDiff();
        if (queryGenerators.size() == 0) {
            return null;
        }

        final XmlGenerator.DBType dbtype = XmlGenerator.DBType.fromString(System.getProperty("dbtype"));
        List<String> migrationXmlList = new ArrayList<>(new XmlGenerator(determineVersion(), queryGenerators).generate(dbtype));

        VelocityContext vc = new VelocityContext();
        vc.put("baseTag", "migrations");
        vc.put("migrationXmlList", migrationXmlList);
        return vc;
    }

    private int determineVersion() {
        int version = 1;

        for (Migration m : mr.orderedMigrations()) {
            if (m.getDbVersion() >= version) {
                version = m.getDbVersion() + 1;
            }
        }

        return version;
    }

    private String getRelativeFileName() {
        return date.getTime() + ".migration";
    }

    private PriorityQueue<QueryGenerator> analyzeDiff() {
        MigrationContext mc = new MigrationContext(mr);
        printMessage(Diagnostic.Kind.NOTE, "size of mc.allTables() = " + mc.allTables().size());
        for (TableInfo table : mc.allTables()) {
            printMessage(Diagnostic.Kind.NOTE, "analyzeDiff table: " + table.toString());
        }

        PriorityQueue<QueryGenerator> retQueue = new PriorityQueue<>();
        for (TableInfo table : allTables) {
            if (!mc.hasTable(table.getTableName())) {
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
                continue;
            }
            TableInfo mcTable = mc.getTable(table.getTableName());
            for (ColumnInfo column : table.getNonForeignKeyColumns()) {
                if ("_id".equals(column.getColumnName())) {
                    continue;
                }
                if (!mcTable.hasColumn(column.getColumnName())) {
                    printMessage(Diagnostic.Kind.NOTE, table.getTableName() + "." + column.getColumnName() +" column did not previously exist. Creating migration for it");
                    retQueue.add(new AddColumnGenerator(table.getTableName(), column));
                }
            }
            for (ColumnInfo column : table.getForeignKeyColumns()) {
                if ("_id".equals(column.getColumnName())) {
                    continue;
                }
                if (!mcTable.hasColumn(column.getColumnName())) {
                    printMessage(Diagnostic.Kind.NOTE, table.getTableName() + "." + column.getColumnName() +" foreign key column did not previously exist. Creating foreign key migration for it");
                    retQueue.add(new AddForeignKeyGenerator(table, column));
                }
            }
        }

        return retQueue;
    }

    private static final class MigrationFileRetriever implements MigrationRetriever.FileRetriever {

        private final File migrationDirectory;

        public MigrationFileRetriever(String migrationDirectory) {
            this.migrationDirectory = new File(migrationDirectory);
        }

        @Override
        public List<File> files() {
            if (!migrationDirectory.exists()) {
                return Collections.EMPTY_LIST;
            }
            if (!migrationDirectory.isDirectory()) {
                return Collections.EMPTY_LIST;
            }

            List<File> retList = new LinkedList<>();
            for (File f : migrationDirectory.listFiles()) {
                retList.add(f);
            }

            return retList;
        }

        @Override
        public File migrationDirectory() {
            return migrationDirectory;
        }
    }
}
