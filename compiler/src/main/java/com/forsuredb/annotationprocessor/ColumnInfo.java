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

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.ForeignKey;
import com.forsuredb.annotation.PrimaryKey;
import com.forsuredb.annotation.Unique;

import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * <p>
 *     Store information about a column in a table.
 * </p>
 */
public class ColumnInfo implements Comparable<ColumnInfo> {

    private final String methodName;
    private final String columnName;
    private final String qualifiedType;
    private final String defaultValue;
    private final boolean unique;
    private final boolean primaryKey;
    private final ForeignKeyInfo foreignKey;

    private ColumnInfo(String methodName,
                       String columnName,
                       String qualifiedType,
                       String defaultValue,
                       boolean unique,
                       boolean primaryKey,
                       ForeignKeyInfo foreignKey) {
        this.methodName = methodName;
        this.columnName = columnName == null || columnName.isEmpty() ? methodName : columnName;
        this.qualifiedType = qualifiedType == null || qualifiedType.isEmpty() ? "java.lang.String" : qualifiedType;
        this.defaultValue = defaultValue;
        this.unique = unique;
        this.primaryKey = primaryKey;
        this.foreignKey = foreignKey;
    }

    public static ColumnInfo from(ExecutableElement ee) {
        if (ee.getKind() != ElementKind.METHOD) {
            return null;
        }

        Builder builder = new Builder();
        for (AnnotationMirror am : ee.getAnnotationMirrors()) {
            appendAnnotationInfo(builder, am);
        }

        return builder.methodName(ee.getSimpleName().toString())
                      .qualifiedType(ee.getReturnType().toString())
                      .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return new StringBuffer("ColumnInfo{columnName=").append(columnName)
                .append(", methodName=").append(methodName)
                .append(", qualifiedType=").append(qualifiedType)
                .append(", foreignKey=").append(foreignKey == null ? "null" : foreignKey.toString())
                .append(", unique=").append(unique)
                .append(", primaryKey=").append(primaryKey)
                .append("}").toString();
    }

    @Override
    public int compareTo(ColumnInfo other) {
        // handle null cases
        if (other == null || other.getColumnName() == null) {
            return -1;
        }
        if (columnName == null) {
            return 1;
        }

        // prioritize default columns
        if (TableInfo.DEFAULT_COLUMNS.containsKey(columnName) && !TableInfo.DEFAULT_COLUMNS.containsKey(other.getColumnName())) {
            return -1;
        }
        if (!TableInfo.DEFAULT_COLUMNS.containsKey(columnName) && TableInfo.DEFAULT_COLUMNS.containsKey(other.getColumnName())) {
            return 1;
        }

        // prioritize foreign key columns
        if (isForeignKey() && !other.isForeignKey()) {
            return -1;  // <-- this column is a foreign key and the other is not
        }
        if (!isForeignKey() && other.isForeignKey()) {
            return 1;   // <-- this column is not a foreign key and the other is
        }

        return columnName.compareToIgnoreCase(other.getColumnName());
    }

    public String getMethodName() {
        return methodName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getQualifiedType() {
        return qualifiedType;
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isForeignKey() {
        return foreignKey != null;
    }

    public ForeignKeyInfo getForeignKey() {
        return foreignKey;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }

    /**
     * <p>
     *     Allows the tables to know the name of the foreign key class without resorting to the trickery you see
     *     here: http://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor
     * </p>
     * @param allTables
     */
    /*package*/ void enrichWithForeignTableInfo(List<TableInfo> allTables) {
        if (!isForeignKey()) {
            return;
        }
        setForeignKeyTableName(allTables);
    }

    private String setForeignKeyTableName(List<TableInfo> allTables) {
        for (TableInfo table : allTables) {
            if (table.getQualifiedClassName().equals(foreignKey.getForeignKeyApiClassName())) {
                foreignKey.setForeignKeyTableName(table.getTableName());
                break;
            }
        }

        return null;
    }

    private static void appendAnnotationInfo(Builder builder, AnnotationMirror am) {
        AnnotationTranslator at = AnnotationTranslatorFactory.inst().create(am);

        String annotationClass = am.getAnnotationType().toString();
        if (annotationClass.equals(FSColumn.class.getName())) {
            builder.columnName(at.property("value").as(String.class));
        } else if (annotationClass.equals(ForeignKey.class.getName())) {
            builder.foreignKey(ForeignKeyInfo.builder().foreignKeyColumnName(at.property("columnName").as(String.class))
                    .foreignKeyApiClassName(at.property("apiClass").uncasted().toString())
                    .cascadeDelete(at.property("cascadeDelete").as(boolean.class))
                    .cascadeUpdate(at.property("cascadeUpdate").as(boolean.class))
                    .build());
        } else if (annotationClass.equals(PrimaryKey.class.getName())) {
            builder.primaryKey(true);
        } else if (annotationClass.equals(Unique.class.getName())) {
            builder.unique(true);
        }
    }

    public static class Builder {

        private String methodName;
        private String columnName;
        private String qualifiedType;
        private String defaultValue;
        private boolean unique = false;
        private boolean primaryKey = false;
        private ForeignKeyInfo foreignKey;

        private Builder() {}

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public Builder qualifiedType(TypeMirror typeMirror) {
            qualifiedType = typeMirror.toString();
            return this;
        }

        public Builder qualifiedType(String qualifiedType) {
            this.qualifiedType = qualifiedType;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder foreignKey(ForeignKeyInfo foreignKey) {
            this.foreignKey = foreignKey;
            return this;
        }

        public Builder primaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
            return this;
        }

        public Builder unique(boolean unique) {
            this.unique = unique;
            return this;
        }

        public ColumnInfo build() {
            if (!canBuild()) {
                throw new IllegalStateException("Cannot build ColumnInfo if both methodName and columnName are null/empty");
            }
            return new ColumnInfo(methodName,
                    columnName,
                    qualifiedType,
                    defaultValue,
                    unique,
                    primaryKey,
                    foreignKey);
        }

        private boolean canBuild() {
            return (methodName != null && !methodName.isEmpty())
                    || (columnName != null && !columnName.isEmpty());
        }
    }
}
