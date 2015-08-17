package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.Migration;
import com.forsuredb.migration.MigrationRetriever;

import org.apache.velocity.VelocityContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.FileObject;

/*package*/ class MigrationGenerator extends BaseGenerator<FileObject> {

    private final Date date;
    private final List<TableInfo> allTables;
    private final MigrationContext mc;

    public MigrationGenerator(List<TableInfo> allTables, String migrationDirectory, ProcessingEnvironment processingEnv)  {
        super(processingEnv);
        date = new Date();
        this.allTables = allTables;
        final MigrationRetriever mr = new MigrationRetriever(new MigrationFileRetriever(migrationDirectory));
        mc = new MigrationContext(mr);
    }

    @Override
    protected FileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return new ResourceCreator(getRelativeFileName()).create(processingEnv);
    }

    @Override
    protected VelocityContext createVelocityContext() {
        final XmlGenerator.DBType dbtype = XmlGenerator.DBType.fromString(System.getProperty("dbtype"));
        // TODO: make it dbVersion-dependent and perform more than just creates
        List<String> migrationXmlList = new ArrayList<>(new XmlGenerator(1, allTables).generate(dbtype));

        VelocityContext vc = new VelocityContext();
        vc.put("baseTag", "migrations");
        vc.put("migrationXmlList", migrationXmlList);
        return vc;
    }

    private String getRelativeFileName() {
        return date.getTime() + ".migration";
    }

    private void analyzeDiff(ProcessingEnvironment processingEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "analyzing diff");
        List<TableInfo> mcTables = mc.allTables();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "size of mc.allTables() = " + mc.allTables().size());
        for (TableInfo table : mcTables) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "analyzeDiff table: " + table.toString());
        }
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
