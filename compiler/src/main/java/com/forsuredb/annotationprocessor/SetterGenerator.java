package com.forsuredb.annotationprocessor;

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

/*package*/ class SetterGenerator extends BaseGenerator<JavaFileObject> {

    private final TableInfo tableInfo;
    private final String resultParameter;

    public SetterGenerator(TableInfo tableInfo, String resultParameter, ProcessingEnvironment processingEnv) {
        super(processingEnv);
        this.tableInfo = tableInfo;
        this.resultParameter = resultParameter;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    @Override
    protected VelocityContext createVelocityContext() {
        VelocityContext vc = new VelocityContext();
        if (resultParameter != null && !resultParameter.isEmpty()) {
            vc.put("resultParameter", resultParameter);
        }
        vc.put("className", getOutputClassName(false));
        vc.put("packageName", tableInfo.getPackageName());
        vc.put("methodDefinitions", createMethodDefinitions());
        return vc;
    }

    private List<String> createMethodDefinitions() {
        List<String> retList = new ArrayList<>();
        for (ColumnInfo column : tableInfo.getColumns()) {
            retList.add(new StringBuilder("@FSColumn(\"").append(column.getColumnName())
                                                         .append("\") ") .append(getOutputClassName(false))
                                                         .append(" ") .append(column.getMethodName())
                                                         .append("(").append(column.getQualifiedType().toString())
                                                         .append(" ").append(column.getMethodName()).append(");")
                                                         .toString());
        }

        return retList;
    }

    private String getOutputClassName(boolean fullyQualified) {
        return (fullyQualified ? tableInfo.getQualifiedClassName() : tableInfo.getSimpleClassName()) + "Setter";
    }
}
