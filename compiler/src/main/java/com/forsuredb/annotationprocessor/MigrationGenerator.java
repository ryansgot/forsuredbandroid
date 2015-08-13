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
    private final TableInfo tableInfo;
    private final List<TableInfo> allTables;

    public MigrationGenerator(TableInfo tableInfo, List<TableInfo> allTables, ProcessingEnvironment processingEnv)  {
        super(processingEnv);
        date = new Date();
        this.tableInfo = tableInfo;
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
        XmlGenerator xml = XmlGenerator.builder().dbVersion(1)
                                                 .keyword(XmlGenerator.Keyword.CREATE)
                                                 .tableInfo(tableInfo)
                                                 .build();

        List<String> migrationXmlList = new ArrayList<>();
        migrationXmlList.add(xml.generate(dbtype, allTables));

        VelocityContext vc = new VelocityContext();
        vc.put("baseTag", "migrations");
        vc.put("migrationXmlList", migrationXmlList);
        return vc;
    }

    private String getRelativeFileName() {
        return date.getTime() + "_" + tableInfo.getTableName() + ".migration";
    }
}
