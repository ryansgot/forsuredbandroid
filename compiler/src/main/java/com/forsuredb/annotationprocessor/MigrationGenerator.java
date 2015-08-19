package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.Migration;
import com.forsuredb.migration.MigrationContext;
import com.forsuredb.migration.MigrationRetriever;
import com.forsuredb.migration.MigrationRetrieverFactory;
import com.forsuredb.migration.QueryGenerator;

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
        mr = new MigrationRetrieverFactory().fromDirectory(migrationDirectory);
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

        List<String> migrationXmlList = new ArrayList<>(new XmlGenerator(determineVersion(), queryGenerators).generate());

        VelocityContext vc = new VelocityContext();
        vc.put("baseTag", "migrations");
        vc.put("migrationXmlList", migrationXmlList);
        return vc;
    }

    private int determineVersion() {
        int version = 1;

        for (Migration m : mr.getMigrations()) {
            if (m.getDbVersion() >= version) {
                version = m.getDbVersion() + 1;
            }
        }

        return version;
    }

    private String getRelativeFileName() {
        return date.getTime() + ".migration";
    }
}
