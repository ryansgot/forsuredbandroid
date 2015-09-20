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
    private final String applicationPackageName;
    private final String resultParameter;

    public ForSureGenerator(Collection<TableInfo> allTables, String applicationPackageName, String resultParameter, ProcessingEnvironment processingEnv) {
        super("forsure.vm", processingEnv);
        this.allTables = allTables;
        this.applicationPackageName = applicationPackageName;
        this.resultParameter = resultParameter;
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
        vc.put("tableResolvers", createTableResolvers());
        return vc;
    }

    private List<String> createModelClassImports() {
        List<String> ret = new ArrayList<>();
        for (TableInfo table : allTables) {
            ret.add(table.getQualifiedClassName());
        }
        return ret;
    }

    private List<TableResolver> createTableResolvers() {
        List<TableResolver> ret = new ArrayList<>();
        for (TableInfo table : allTables) {
            ret.add(new TableResolver(table, resultParameter));
        }
        return ret;
    }

    private String getOutputClassName(boolean fullyQualified) {
        return fullyQualified ? applicationPackageName + "." + CLASS_NAME : CLASS_NAME;
    }

    private static class TableResolver {

        private final String tableName;
        private final String getApiClass;
        private final String setApiClass;
        private final String finderClass;
        private final String resolverParametrization;

        public TableResolver(TableInfo table, String resultParameter) {
            this.tableName = table.getTableName();
            getApiClass = table.getSimpleClassName();
            setApiClass = getApiClass + "Setter";
            finderClass = getApiClass + "Finder";
            resolverParametrization = createResolverParameterization(resultParameter);
        }

        private String createResolverParameterization(String resultParameter) {
            return new StringBuilder("Resolver<").append(resultParameter)
                    .append(", ").append(getApiClass)
                    .append(", ").append(setApiClass)
                    .append(", ").append(finderClass)
                    .append(">").toString();
        }

        @Override
        public String toString() {
            return new StringBuilder(doc()).append(outerMethodSignature()).append(" {").append(newLine(1))
                    .append("return new ").append(resolverParametrization).append("() {").append(newLine(2))
                    .append(newLine(3))
                    .append("public static final String TABLE_NAME = \"").append(tableName).append("\";").append(newLine(3))
                    .append(newLine(3))
                    .append("private ").append(getApiClass).append(" getApi;").append(newLine(3))
                    .append("private ").append(finderClass).append(" finder;").append(newLine(3))
                    .append(newLine(3))
                    .append("@Override").append(newLine(3))
                    .append("public ").append(getApiClass).append(" getApi() {").append(newLine(4))
                    .append("if (getApi == null) {").append(newLine(5))
                    .append("getApi = FSGetAdapter.create(").append(getApiClass).append(".class);").append(newLine(4))
                    .append("}").append(newLine(4))
                    .append("return getApi;").append(newLine(3))
                    .append("}").append(newLine(3))
                    .append(newLine(3))
                    .append("@Override").append(newLine(3))
                    .append("public Retriever get() {").append(newLine(4))
                    .append("FSSelection selection = finder == null ? new FSSelection.SelectAll() : finder.selection();").append(newLine(4))
                    .append("finder = null;  // <-- When a finder's selection method is called, it must be nullified").append(newLine(4))
                    .append("return instance.infoFactory.createQueryable(instance.resourceOf(TABLE_NAME)).query(null, selection, null);").append(newLine(3))
                    .append("}").append(newLine(3))
                    .append(newLine(3))
                    .append("@Override").append(newLine(3))
                    .append("public ").append(setApiClass).append(" set() {").append(newLine(4))
                    .append("FSQueryable queryable = instance.infoFactory.createQueryable(instance.resourceOf(TABLE_NAME));").append(newLine(4))
                    .append("RecordContainer recordContainer = instance.infoFactory.createRecordContainer();").append(newLine(4))
                    .append("FSSelection selection = finder == null ? null : finder.selection();").append(newLine(4))
                    .append("finder = null;  // <-- When a finder's selection method is called, it must be nullified").append(newLine(4))
                    .append("return FSSaveAdapter.create(queryable, selection, recordContainer, ").append(setApiClass).append(".class);").append(newLine(3))
                    .append("}").append(newLine(3))
                    .append(newLine(3))
                    .append("@Override").append(newLine(3))
                    .append("public ").append(finderClass).append(" find() {").append(newLine(4))
                    .append("finder = new ").append(finderClass).append("(this);").append(newLine(4))
                    .append("return finder;").append(newLine(3))
                    .append("}").append(newLine(2))
                    .append("};").append(newLine(1))
                    .append("}")
                    .toString();
        }

        private String outerMethodSignature() {
            return new StringBuilder("public static ").append(resolverParametrization)
                    .append(" ").append(WordUtils.uncapitalize(getApiClass))
                    .append("()").toString();
        }

        private String doc() {
            return new StringBuilder("/**").append(newLine(1))
                    .append(" * Access the querying mechanisms for the ").append(tableName).append(" table.").append(newLine(1))
                    .append(" * @see ").append(getApiClass).append(newLine(1))
                    .append(" * @see ").append(setApiClass).append(newLine(1))
                    .append(" * @see ").append(finderClass).append(newLine(1))
                    .append(" */").append(newLine(1))
                    .toString();
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
