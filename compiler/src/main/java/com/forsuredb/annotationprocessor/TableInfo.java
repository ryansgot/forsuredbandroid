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

import com.forsuredb.annotation.FSTable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

public class TableInfo {

    // The default columns for each table
    public static final Map<String, ColumnInfo> DEFAULT_COLUMNS = new HashMap<>();
    static {
        DEFAULT_COLUMNS.put("_id", ColumnInfo.builder().columnName("_id")
                .methodName("id")
                .primaryKey(true)
                .qualifiedType("long")
                .build());
        DEFAULT_COLUMNS.put("created", ColumnInfo.builder().columnName("created")
                .methodName("created")
                .qualifiedType("java.util.Date")
                .defaultValue("CURRENT_TIMESTAMP")
                .build());
        DEFAULT_COLUMNS.put("modified", ColumnInfo.builder().columnName("modified")
                .methodName("modified")
                .qualifiedType("java.util.Date")
                .defaultValue("CURRENT_TIMESTAMP")
                .build());
        DEFAULT_COLUMNS.put("deleted", ColumnInfo.builder().columnName("deleted")
                .methodName("deleted")
                .qualifiedType("boolean")
                .defaultValue("0")
                .build());
    }

    private final Map<String, ColumnInfo> columnMap = new HashMap<>();
    private final String qualifiedClassName;
    private final String simpleClassName;
    private final String classPackageName;
    private final String tableName;

    private TableInfo(String tableName, String qualifiedClassName, Map<String, ColumnInfo> columnMap) {
        this.tableName = createTableName(tableName, qualifiedClassName);
        this.qualifiedClassName = qualifiedClassName;
        this.columnMap.putAll(DEFAULT_COLUMNS);
        this.columnMap.putAll(columnMap);
        this.simpleClassName = createSimpleClassName(qualifiedClassName);
        this.classPackageName = createPackageName(qualifiedClassName);
    }

    public static TableInfo from(TypeElement intf) {
        if (intf == null) {
            throw new IllegalArgumentException("Cannot create TableInfo from null TypeElement");
        }
        if (intf.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Can only create TableInfo from " + ElementKind.INTERFACE.toString() + ", not " + intf.getKind().toString());
        }

        Builder builder = new Builder();
        for (ExecutableElement me : ElementFilter.methodsIn(intf.getEnclosedElements())) {
            builder.addColumn(ColumnInfo.from(me));
        }
        return builder.qualifiedClassName(intf.getQualifiedName().toString())
                      .tableName(createTableName(intf))
                      .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return new StringBuffer("TableInfo {\n\ttableName=").append(tableName)
                                                            .append("\n\tqualifiedClassName=").append(qualifiedClassName)
                                                            .append("\n\tsimpleClassName=").append(simpleClassName)
                                                            .append("\n\tclassPackageName=").append(classPackageName)
                                                            .append("\n\tcolumns=").append(columnMap.toString())
                                                            .append("\n}").toString();
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public String getPackageName() {
        return classPackageName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean hasColumn(String columnName) {
        return columnMap.containsKey(columnName);
    }

    public ColumnInfo getColumn(String columnName) {
        return columnName == null ? null : columnMap.get(columnName);
    }

    public Collection<ColumnInfo> getColumns() {
        return columnMap.values();
    }

    public List<ColumnInfo> getForeignKeyColumns() {
        List<ColumnInfo> retList = new LinkedList<>();
        for (ColumnInfo column : getColumns()) {
            if (column.isForeignKey()) {
                retList.add(column);
            }
        }
        Collections.sort(retList);
        return retList;
    }

    public List<ColumnInfo> getNonForeignKeyColumns() {
        List<ColumnInfo> retList = new LinkedList<>();
        for (ColumnInfo column : getColumns()) {
            if (!column.isForeignKey()) {
                retList.add(column);
            }
        }
        Collections.sort(retList);
        return retList;
    }

    private String createSimpleClassName(String qualifiedClassName) {
        if (qualifiedClassName == null || qualifiedClassName.isEmpty()) {
            return null;
        }
        String[] split = qualifiedClassName.split("\\.");
        return split[split.length - 1];
    }

    private String createTableName(String tableName, String qualifiedClassName) {
        if (tableName != null && !tableName.isEmpty()) {
            return tableName;
        }
        String[] split = qualifiedClassName.split("\\.");
        return split[split.length - 1];
    }

    private static String createTableName(TypeElement intf) {
        FSTable table = intf.getAnnotation(FSTable.class);
        return table == null ? intf.getSimpleName().toString() : table.value();
    }

    private String createPackageName(String qualifiedClassName) {
        if (qualifiedClassName == null || qualifiedClassName.isEmpty()) {
            return null;
        }
        String[] split = qualifiedClassName.split("\\.");
        StringBuffer buf = new StringBuffer(split[0]);
        for (int i = 1; i < split.length - 1; i++) {
            buf.append(".").append(split[i]);
        }
        return buf.toString();
    }

    public static class Builder {
        private final Map<String, ColumnInfo> columnMap = new HashMap<>();
        private String qualifiedClassName;
        private String tableName;

        public Builder addColumn(ColumnInfo column) {
            if (column != null) {
                columnMap.put(column.getColumnName(), column);
            }
            return this;
        }

        public Builder qualifiedClassName(String qualifiedClassName) {
            this.qualifiedClassName = qualifiedClassName;
            return this;
        }

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public TableInfo build() {
            if (!canBuild()) {
                throw new IllegalStateException("Cannot build TableInfo with both qualifiedClassName and tableName null/empty");
            }
            return new TableInfo(tableName, qualifiedClassName, columnMap);
        }

        private boolean canBuild() {
            return (qualifiedClassName != null && !qualifiedClassName.isEmpty()) || (tableName != null && !tableName.isEmpty());
        }
    }
}
