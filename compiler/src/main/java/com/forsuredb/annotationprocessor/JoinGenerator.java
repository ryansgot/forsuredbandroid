package com.forsuredb.annotationprocessor;

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

public class JoinGenerator extends BaseGenerator<JavaFileObject> {

    private final JoinInfo join;
    private final String resultParameter;

    public JoinGenerator(JoinInfo join, String resultParameter, ProcessingEnvironment processingEnv) {
        super("join.vm", processingEnv);
        this.join = join;
        this.resultParameter = resultParameter;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    @Override
    protected VelocityContext createVelocityContext() {
        VelocityContext vc = new VelocityContext();
        vc.put("packageName", join.getChildTable().getPackageName());
        vc.put("resultParameter", resultParameter);
        vc.put("parentApiClassImport", join.getParentTable().getQualifiedClassName());
        vc.put("parentColumnNames", getColumnNames(join.getParentTable()));
        vc.put("childColumnNames", getColumnNames(join.getChildTable()));
        vc.put("parentGetApiClass", join.getParentTable().getSimpleClassName());
        vc.put("parentTableName", join.getParentTable().getTableName());
        vc.put("parentColumnName", join.getParentColumn().getColumnName());
        vc.put("childGetApiClass", join.getChildTable().getSimpleClassName());
        vc.put("childTableName", join.getChildTable().getTableName());
        vc.put("childColumnName", join.getChildColumn().getColumnName());
        vc.put("className", getOutputClassName(false));
        return vc;
    }

    private List<String> getColumnNames(TableInfo table) {
        List<String> ret = new ArrayList<>();
        for (ColumnInfo column : table.getColumns()) {
            ret.add(column.getColumnName());
        }
        return ret;
    }

    private String getOutputClassName(boolean fullyQualified) {
        return (fullyQualified ? join.getChildTable().getQualifiedClassName() : join.getChildTable().getSimpleClassName()) + "Join" + join.getParentTable().getSimpleClassName();
    }
}
