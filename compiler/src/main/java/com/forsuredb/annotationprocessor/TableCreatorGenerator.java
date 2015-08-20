package com.forsuredb.annotationprocessor;

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

public class TableCreatorGenerator extends BaseGenerator<JavaFileObject> {

    private static final String CLASS_NAME = "TableGenerator";
    private static final String METHOD_NAME = "generate";
    private static final String LIST_VARIABLE_NAME = "retList";

    private final String applicationPackageName;
    private final ProcessingContext pContext;

    public TableCreatorGenerator(ProcessingEnvironment processingEnv, String applicationPackageName, ProcessingContext pContext) {
        super(processingEnv);
        this.applicationPackageName = applicationPackageName;
        this.pContext = pContext;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    @Override
    protected VelocityContext createVelocityContext() {
        VelocityContext vc = new VelocityContext();
        vc.put("className", getOutputClassName(false));
        vc.put("packageName", applicationPackageName);
        vc.put("methodName", METHOD_NAME);
        vc.put("listVariableName", LIST_VARIABLE_NAME);
        vc.put("addFSTableCreatorLines", createAddFSTableCratorLines());
        return vc;
    }

    private String getOutputClassName(boolean fullyQualified) {
        return fullyQualified ? applicationPackageName + "." + CLASS_NAME : CLASS_NAME;
    }

    private List<String> createAddFSTableCratorLines() {
        List<String> retList = new ArrayList<>();
        for (TableInfo table : pContext.allTables()) {
            retList.add(createAddFSTableCreatorLine(table));
        }
        return retList;
    }

    private String createAddFSTableCreatorLine(TableInfo tableInfo) {
        return new StringBuffer(LIST_VARIABLE_NAME).append(".add(new FSTableCreator(")
                .append(tableInfo.getQualifiedClassName())
                .append(".class));")
                .toString();
    }
}
