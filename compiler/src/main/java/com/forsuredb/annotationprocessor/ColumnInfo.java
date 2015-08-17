package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.ForeignKey;
import com.forsuredb.annotation.PrimaryKey;
import com.forsuredb.annotation.Unique;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

public class ColumnInfo {

    private final String methodName;
    private final String columnName;
    private final String qualifiedType;
    private final boolean foreignKey;
    private final boolean unique;
    private final boolean primaryKey;
    private String foreignKeyTableName;   // <-- cannot be known at creation time in some cases
    private String foreignKeyColumnName;  // <-- cannot be known at creation time in some cases
    private String foreignKeyApiClassName;

    private ColumnInfo(String methodName,
                       String columnName,
                       String qualifiedType,
                       boolean foreignKey,
                       boolean unique,
                       boolean primaryKey,
                       String foreignKeyTableName,
                       String foreignKeyColumnName,
                       String foreignKeyApiClassName) {
        this.methodName = methodName;
        this.columnName = columnName == null || columnName.isEmpty() ? methodName : columnName;
        this.qualifiedType = qualifiedType == null || qualifiedType.isEmpty() ? "java.lang.String" : qualifiedType;
        this.foreignKey = foreignKey;
        this.unique = unique;
        this.primaryKey = primaryKey;
        this.foreignKeyTableName = foreignKeyTableName;
        this.foreignKeyColumnName = foreignKeyColumnName;
        this.foreignKeyApiClassName = foreignKeyApiClassName;
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
                .append(", foreignKey=").append(foreignKey)
                .append(", unique=").append(unique)
                .append(", primaryKey=").append(primaryKey)
                .append(", foreignKeyTableName=").append(foreignKeyTableName)
                .append(", foreignKeyColumnName=").append(getForeignKeyColumnName())
                .append(", foreignKeyApiClassName=").append(foreignKeyApiClassName)
                .append("}").toString();
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

    public boolean isForeignKey() {
        return foreignKey;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }

    public String getForeignKeyTableName() {
        return foreignKeyTableName;
    }

    public String getForeignKeyColumnName() {
        return foreignKeyColumnName;
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
            if (table.getQualifiedClassName().equals(foreignKeyApiClassName)) {
                foreignKeyTableName = table.getTableName();
                break;
            }
        }

        return null;
    }

    private static void appendAnnotationInfo(Builder builder, AnnotationMirror am) {
        AnnotationTranslator at = new AnnotationTranslator(am.getElementValues());

        String annotationClass = am.getAnnotationType().toString();
        if (annotationClass.equals(FSColumn.class.getName())) {
            builder.columnName(at.property("value").as(String.class));
        } else if (annotationClass.equals(ForeignKey.class.getName())) {
            builder.foreignKeyColumnName(at.property("columnName").as(String.class))
                    .foreignKeyApiClassName(at.property("apiClass").uncasted().toString())
                    .foreignKey(true);
        } else if (annotationClass.equals(PrimaryKey.class.getName())) {
            builder.primaryKey(true);
        } else if (annotationClass.equals(Unique.class.getName())) {
            builder.unique(true);
        }
    }

    /*package*/ static class Builder {

        private String methodName;
        private String columnName;
        private String qualifiedType;
        private boolean foreignKey = false;
        private boolean unique = false;
        private boolean primaryKey = false;
        private String foreignKeyTableName;
        private String foreignKeyColumnName;
        private String foreignKeyApiClassName;

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

        public Builder foreignKeyTableName(String foreignKeyTableName) {
            this.foreignKeyTableName = foreignKeyTableName;
            return this;
        }

        public Builder foreignKeyColumnName(String foreignKeyColumnName) {
            this.foreignKeyColumnName = foreignKeyColumnName;
            return this;
        }

        public Builder foreignKeyApiClassName(String foreignKeyApiClassName) {
            this.foreignKeyApiClassName = foreignKeyApiClassName;
            return this;
        }

        public Builder foreignKey(boolean foreignKey) {
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
                    foreignKey,
                    unique,
                    primaryKey,
                    foreignKeyTableName,
                    foreignKeyColumnName,
                    foreignKeyApiClassName);
        }

        private boolean canBuild() {
            return (methodName != null && !methodName.isEmpty())
                    || (columnName != null && !columnName.isEmpty());
        }
    }
}