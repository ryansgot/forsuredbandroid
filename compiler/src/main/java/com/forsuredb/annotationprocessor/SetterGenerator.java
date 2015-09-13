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

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

/**
 * <p>
 *     Takes the table info (which was generated from an {@link com.forsuredb.api.FSGetApi FSGetApi}
 *     extension) and  will generate the corresponding {@link com.forsuredb.api.FSSaveApi FSSaveApi}
 *     interface.
 * </p>
 */
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
            retList.add(createMethodDefinition(column));
        }

        return retList;
    }

    private String getOutputClassName(boolean fullyQualified) {
        return (fullyQualified ? tableInfo.getQualifiedClassName() : tableInfo.getSimpleClassName()) + "Setter";
    }

    private String createMethodDefinition(ColumnInfo column) {
        return new StringBuilder("@FSColumn(\"").append(column.getColumnName())
                .append("\") ") .append(getOutputClassName(false))
                .append(" ") .append(column.getMethodName())
                .append("(").append(column.getQualifiedType().toString())
                .append(" ").append(column.getMethodName()).append(");")
                .toString();
    }
}
