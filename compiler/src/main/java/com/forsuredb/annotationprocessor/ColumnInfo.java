package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.ForeignKey;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

public class ColumnInfo {

    private final String methodName;
    private final MetaData metaData;
    private final String columnName;
    private final TypeMirror type;
    private String foreignKeyTableName;   // <-- cannot be known at creation time

    private ColumnInfo(String methodName, MetaData metaData, TypeMirror type) {
        this.methodName = methodName;
        this.metaData = metaData;
        this.type = type;
        columnName = createColumnName();
    }

    public static ColumnInfo from(ExecutableElement ee) {
        if (ee.getKind() != ElementKind.METHOD) {
            return null;
        }
        return builder().metaData(new MetaData(ee.getAnnotationMirrors()))
                        .methodName(ee.getSimpleName().toString())
                        .type(ee.getReturnType())
                        .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return new StringBuffer("ColumnInfo{columnName=").append(columnName)
                                                         .append("; methodName=").append(methodName)
                                                         .append("; type=").append(type.toString())
                                                         .append("; foreignKeyTableName=").append(foreignKeyTableName)
                                                         .append("; foreignKeyColumnName=").append(getForeignKeyColumnName())
                                                         .append("; metaData=").append(metaData.toString())
                                                         .append("}").toString();
    }

    public String getMethodName() {
        return methodName;
    }

    public String getColumnName() {
        return columnName;
    }

    public TypeMirror getType() {
        return type;
    }

    public boolean isForeignKey() {
        return metaData.isAnnotationPresent(ForeignKey.class);
    }

    /**
     * @return the name of the table containing the foreign key column or null if not a foreign key
     */
    public String getForeignKeyColumnName() {
        if (!isForeignKey()) {
            return null;
        }
        return getAnnotation(ForeignKey.class).property("columnName").as(String.class);
    }

    /**
     * @return the name of foreign key column contained within the foreign key table or null if not a foreign key
     */
    public String getForeignKeyTableName() {
        if (!isForeignKey()) {
            return null;
        }
        return foreignKeyTableName;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationCls) {
        return metaData.isAnnotationPresent(annotationCls);
    }

    public MetaData.AnnotationTranslator getAnnotation(Class<? extends Annotation> annotationCls) {
        return metaData.get(annotationCls);
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
        foreignKeyTableName = createForeignKeyTableName(allTables);
    }

    private String createForeignKeyTableName(List<TableInfo> allTables) {
        Object uncasted = getAnnotation(ForeignKey.class).property("apiClass").uncasted();
        for (TableInfo table : allTables) {
            if (table.getQualifiedClassName().equals(uncasted.toString())) {
                return table.getTableName();
            }
        }

        return null;
    }

    private String createColumnName() {
        if (!metaData.isAnnotationPresent(FSColumn.class)) {
            return methodName;
        }

        String columnName = metaData.get(FSColumn.class).property("value").as(String.class);
        return columnName == null || columnName.isEmpty() ? methodName : columnName;
    }

    /*package*/ static class Builder {

        private MetaData metaData;
        private String methodName;
        private TypeMirror type;

        private Builder() {}

        public Builder metaData(MetaData metaData) {
            this.metaData = metaData;
            return this;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder type(TypeMirror type) {
            this.type = type;
            return this;
        }

        public ColumnInfo build() {
            if (!canBuild()) {
                return null;
            }
            return new ColumnInfo(methodName, metaData, type);
        }

        private boolean canBuild() {
            return methodName != null && !methodName.isEmpty();
        }
    }
}
