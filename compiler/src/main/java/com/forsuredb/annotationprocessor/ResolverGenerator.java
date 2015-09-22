package com.forsuredb.annotationprocessor;

import org.apache.velocity.VelocityContext;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

public class ResolverGenerator extends BaseGenerator<JavaFileObject> {

    private final TableInfo table;
    private final String resultParameter;

    public ResolverGenerator(TableInfo table, String resultParameter, ProcessingEnvironment processingEnv) {
        super("resolver.vm", processingEnv);
        this.table = table;
        this.resultParameter = resultParameter;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    @Override
    protected VelocityContext createVelocityContext() {
        VelocityContext vc = new VelocityContext();
        vc.put("packageName", table.getPackageName());
        vc.put("resultParameter", resultParameter);
        vc.put("getApiClass", table.getSimpleClassName());
        vc.put("className", getOutputClassName(false));
        vc.put("setApiClass", setApiClass());
        vc.put("finderClass", finderClass());
        vc.put("tableName", table.getTableName());
        return vc;
    }

    private String getOutputClassName(boolean fullyQualified) {
        return (fullyQualified ? table.getQualifiedClassName() : table.getSimpleClassName()) + "Resolver";
    }

    private String setApiClass() {
        return table.getSimpleClassName() + "Setter";
    }

    private String finderClass() {
        return table.getSimpleClassName() + "Finder";
    }
}
