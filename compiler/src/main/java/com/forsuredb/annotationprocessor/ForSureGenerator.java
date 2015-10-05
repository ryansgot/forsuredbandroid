/*
   forsuredb, an object relational mapping tool

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.forsuredb.annotationprocessor;

import org.apache.commons.lang.WordUtils;
import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

public class ForSureGenerator extends BaseGenerator<JavaFileObject> {

    private static final String CLASS_NAME = "ForSure";

    private final Collection<TableInfo> allTables;
    private final List<JoinResolver> allJoinResolvers;
    private final String applicationPackageName;
    private final String resultParameter;

    public ForSureGenerator(Collection<TableInfo> allTables, List<JoinInfo> allJoins, String applicationPackageName, String resultParameter, ProcessingEnvironment processingEnv) {
        super("forsure.vm", processingEnv);
        this.resultParameter = resultParameter;
        this.allTables = allTables;
        this.allJoinResolvers = createJoinResolvers(allJoins);
        this.applicationPackageName = applicationPackageName;
    }

    @Override
    protected JavaFileObject createFileObject(ProcessingEnvironment processingEnv) throws IOException {
        return processingEnv.getFiler().createSourceFile(getOutputClassName(true));
    }

    @Override
    protected VelocityContext createVelocityContext() {
        final VelocityContext vc = new VelocityContext();
        vc.put("packageName", applicationPackageName);
        vc.put("resultParameter", resultParameter);
        vc.put("modelClassImports", createModelClassImports());
        vc.put("joinClassImports", createJoinClassImports());
        vc.put("tableResolverMethods", createTableResolverMethods());
        vc.put("joinResolvers", allJoinResolvers);
        return vc;
    }

    private List<String> createModelClassImports() {
        List<String> ret = new ArrayList<>();
        for (TableInfo table : allTables) {
            ret.add(table.getQualifiedClassName());
        }
        return ret;
    }

    private List<String> createJoinClassImports() {
        List<String> ret = new ArrayList<>();
        for (JoinResolver joinResolver : allJoinResolvers) {
            ret.add(joinResolver.getFullyQualifiedClassName());
        }
        return ret;
    }

    private List<TableResolver> createTableResolverMethods() {
        List<TableResolver> ret = new ArrayList<>();
        for (TableInfo table : allTables) {
            ret.add(new TableResolver(table, resultParameter));
        }
        return ret;
    }

    private List<JoinResolver> createJoinResolvers(List<JoinInfo> allJoins) {
        List<JoinResolver> ret = new ArrayList<>();
        for (JoinInfo join : allJoins) {
            ret.add(new JoinResolver(join, resultParameter));
        }
        return ret;
    }

    private String getOutputClassName(boolean fullyQualified) {
        return fullyQualified ? applicationPackageName + "." + CLASS_NAME : CLASS_NAME;
    }

    private static class TableResolver {

        private final String resultParameter;
        private final String getApiClass;
        private final String resolverClass;
        private final String setApiClass;
        private final String finderClass;
        private final String tableNameReference;

        public TableResolver(TableInfo table, String resultParameter) {
            this.resultParameter = resultParameter;
            getApiClass = table.getSimpleClassName();
            resolverClass = getApiClass + "Resolver";
            setApiClass = getApiClass + "Setter";
            finderClass = getApiClass + "Finder";
            tableNameReference = resolverClass + ".TABLE_NAME";
        }

        @Override
        public String toString() {
            return new StringBuilder(doc()).append(newLine(1))
                    .append("public static ").append(resolverClass).append(" ").append(WordUtils.uncapitalize(getApiClass)).append("() {").append(newLine(2))
                    .append("return new ").append(resolverClass).append("((").append(resultParameter).append(") instance.resourceOf(").append(tableNameReference).append("), instance.infoFactory);").append(newLine(1))
                    .append("}")
                    .toString();
        }

        private String doc() {
            return new StringBuilder("/**").append(newLine(1))
                    .append(" * Access the querying mechanisms for the {@link ").append(resolverClass).append("#TABLE_NAME").append(" table.").append(newLine(1))
                    .append(" * @see ").append(getApiClass).append(newLine(1))
                    .append(" * @see ").append(setApiClass).append(newLine(1))
                    .append(" * @see ").append(finderClass).append(newLine(1))
                    .append(" * @see ").append(resolverClass).append(newLine(1))
                    .append(" */").toString();
        }

        private String newLine(int tabs) {
            final StringBuilder buf = new StringBuilder("\n");
            for (int i = 0; i < tabs; i++) {
                buf.append("    ");
            }
            return buf.toString();
        }
    }

    private static class JoinResolver {

        private final String className;
        private final String fullyQualifiedClassName;
        private final String methodName;
        private final String parentTableNameReference;
        private final String childTableNameReference;
        private final String resultParameter;

        public JoinResolver(JoinInfo join, String resultParameter) {
            className = join.getChildTable().getSimpleClassName() + "Join" + join.getParentTable().getSimpleClassName();
            fullyQualifiedClassName = join.getChildTable().getPackageName() + "." + className;
            methodName = WordUtils.uncapitalize(className);
            parentTableNameReference = join.getParentTable().getSimpleClassName() + "Resolver.TABLE_NAME";
            childTableNameReference = join.getChildTable().getSimpleClassName() + "Resolver.TABLE_NAME";
            this.resultParameter = resultParameter;
        }

        @Override
        public String toString() {
            return new StringBuilder("public static ").append(className).append(" ").append(methodName).append("() {\n")
                    .append("        ").append("return new ").append(className).append("((").append(resultParameter).append(") instance.resourceOf(").append(parentTableNameReference).append("), (").append(resultParameter).append(") instance.resourceOf(").append(childTableNameReference).append("), ").append("instance.infoFactory);\n")
                    .append("    }")
                    .toString();
        }

        public String getFullyQualifiedClassName() {
            return fullyQualifiedClassName;
        }
    }
}
