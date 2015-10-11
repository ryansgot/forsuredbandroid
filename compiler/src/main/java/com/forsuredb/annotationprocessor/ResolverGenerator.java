package com.forsuredb.annotationprocessor;

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

public class ResolverGenerator extends BaseGenerator<JavaFileObject> {

    private final TableInfo table;
    private final List<JoinInfo> allJoins;
    private final String resultParameter;

    public ResolverGenerator(TableInfo table, List<JoinInfo> allJoins, String resultParameter, ProcessingEnvironment processingEnv) {
        super("resolver.vm", processingEnv);
        this.table = table;
        this.allJoins = allJoins;
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
        vc.put("joinMethodDefinitions", createJoinMethods());
        vc.put("columns", createColumns());
        return vc;
    }

    private List<JoinMethodDefinition> createJoinMethods() {
        List<JoinMethodDefinition> ret = new ArrayList<>();
        for (JoinInfo join : allJoins) {
            if (join.getParentTable().getTableName().equals(table.getTableName())) {
                ret.add(new JoinMethodDefinition(getOutputClassName(false), join, true));
            } else if (join.getChildTable().getTableName().equals(table.getTableName())) {
                ret.add(new JoinMethodDefinition(getOutputClassName(false), join, false));
            }
        }
        return ret;
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

    private List<String> createColumns() {
        List<String> ret = new ArrayList<>();
        for (ColumnInfo column : table.getColumns()) {
            ret.add(column.getColumnName());
        }
        return ret;
    }

    private static class JoinMethodDefinition {

        private final String returnType;
        private final JoinInfo join;
        private final boolean isParent;

        public JoinMethodDefinition(String returnType, JoinInfo join, boolean isParent) {
            this.returnType = returnType;
            this.join = join;
            this.isParent = isParent;
        }

        @Override
        public String toString() {
            return new StringBuilder(doc()).append(newLine(1))
                    .append("public ").append(returnType).append(" ").append(methodName()).append("(final FSJoin.Type type) {").append(newLine(2))
                        .append("projections.add(").append(getOtherResolverClass()).append(".PROJECTION);").append(newLine(2))
                        .append("joins.add(new FSJoin() {").append(newLine(3))
                            .append("@Override").append(newLine(3))
                            .append("public Type type() {").append(newLine(4))
                                .append("return type;").append(newLine(3))
                            .append("}").append(newLine(3))
                            .append("@Override").append(newLine(3))
                            .append("public String parentTable() {").append(newLine(4))
                                .append("return \"").append(join.getParentTable().getTableName()).append("\";").append(newLine(3))
                            .append("}").append(newLine(3))
                            .append("@Override").append(newLine(3))
                            .append("public String parentColumn() {").append(newLine(4))
                                .append("return \"").append(join.getParentColumn().getColumnName()).append("\";").append(newLine(3))
                            .append("}").append(newLine(3))
                            .append("@Override").append(newLine(3))
                            .append("public String childTable() {").append(newLine(4))
                                .append("return \"").append(join.getChildTable().getTableName()).append("\";").append(newLine(3))
                            .append("}").append(newLine(3))
                            .append("@Override").append(newLine(3))
                            .append("public String childColumn() {").append(newLine(4))
                                .append("return \"").append(join.getChildColumn().getColumnName()).append("\";").append(newLine(3))
                            .append("}").append(newLine(2))
                        .append("});").append(newLine(2))
                        .append("return this;").append(newLine(1))
                    .append("}")
                    .toString();
        }

        private String getOtherResolverClass() {
            return (isParent ? join.getChildTable().getSimpleClassName() : join.getParentTable().getSimpleClassName()) + "Resolver";
        }

        private String methodName() {
            return "join" + (isParent ? join.getChildTable().getSimpleClassName() : join.getParentTable().getSimpleClassName());
        }

        private String doc() {
            // TODO
            return "";
        }

        private String newLine(int tabs) {
            final StringBuilder buf = new StringBuilder("\n");
            for (int i = 0; i < tabs; i++) {
                buf.append("    ");
            }
            return buf.toString();
        }
    }
}
