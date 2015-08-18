package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.Migration;
import com.forsuredb.migration.MigrationContext;
import com.forsuredb.migration.MigrationRetriever;
import com.forsuredb.migration.QueryGenerator;

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
import javax.tools.FileObject;

/*package*/ class MigrationGenerator extends BaseGenerator<FileObject> {

    private final Date date;
    private final ProcessingContext pContext;
    private final MigrationRetriever mr;

    public MigrationGenerator(ProcessingContext pContext, String migrationDirectory, ProcessingEnvironment processingEnv)  {
        super(processingEnv);
        date = new Date();
        this.pContext = pContext;
        mr = new MigrationRetriever(new MigrationFileRetriever(migrationDirectory));
    }

    @Override
    protected FileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return new ResourceCreator(getRelativeFileName()).create(processingEnv);
    }

    @Override
    protected VelocityContext createVelocityContext() {
        PriorityQueue<QueryGenerator> queryGenerators = new DiffGenerator(new MigrationContext(mr)).analyzeDiff(pContext);
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
