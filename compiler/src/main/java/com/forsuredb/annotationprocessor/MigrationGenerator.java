package com.forsuredb.annotationprocessor;

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;

/*package*/ class MigrationGenerator extends BaseGenerator<FileObject> {

    private final Date date;
    private final List<TableInfo> allTables;

    public MigrationGenerator(List<TableInfo> allTables, ProcessingEnvironment processingEnv)  {
        super(processingEnv);
        date = new Date();
        this.allTables = allTables;
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
}
