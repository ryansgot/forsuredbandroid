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

import com.forsuredb.api.FSTableCreator;

import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.JavaFileObject;

/**
 * <p>
 *     Generator for the TableGenerator class that contains all of the
 *     {@link FSTableCreator FSTableCreator} instance definitions and returns them as a list.
 * </p>
 * @author Ryan Scott
 */
public class TableCreatorGenerator extends BaseGenerator<JavaFileObject> {

    private static final String CLASS_NAME = "TableGenerator";
    private static final String METHOD_NAME = "generate";
    private static final String LIST_VARIABLE_NAME = "retList";

    private final String applicationPackageName;
    private final ProcessingContext pContext;

    public TableCreatorGenerator(ProcessingEnvironment processingEnv,
                                 String applicationPackageName,
                                 ProcessingContext pContext) {
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
        StringBuffer buf = new StringBuffer(LIST_VARIABLE_NAME).append(".add(new FSTableCreator(")
                .append("authority, ")
                .append(tableInfo.getQualifiedClassName()).append(".class");

        if (tableInfo.hasStaticData()) {
            buf.append(", \"").append(tableInfo.getStaticDataAsset()).append("\"")
                    .append(", \"").append(tableInfo.getStaticDataRecordName()).append("\"");
        }

        for (ColumnInfo column : tableInfo.getForeignKeyColumns()) {
            buf.append(", ").append(column.getForeignKey().getForeignKeyApiClassName()).append(".class");
        }

        return buf.append("));").toString();
    }
}
