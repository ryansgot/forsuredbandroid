package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSTable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

public class TableInfo {

    private final Map<String, ColumnInfo> columnMap = new HashMap<>();
    private final String qualifiedClassName;
    private final String simpleClassName;
    private final String classPackageName;
    private final String tableName;

    private TableInfo(String tableName, String qualifiedClassName, Map<String, ColumnInfo> columnMap) {
        this.tableName = createTableName(tableName, qualifiedClassName);
        this.qualifiedClassName = qualifiedClassName;
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
        return retList;
    }

    public List<ColumnInfo> getNonForeignKeyColumns() {
        List<ColumnInfo> retList = new LinkedList<>();
        for (ColumnInfo column : getColumns()) {
            if (!column.isForeignKey()) {
                retList.add(column);
            }
        }
        return retList;
    }

    private String createSimpleClassName(String qualifiedClassName) {
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
            if (qualifiedClassName == null || qualifiedClassName.isEmpty()) {
                throw new IllegalStateException("Cannot build TableInfo with null or empty qualifiedClassName");
            }
            return new TableInfo(tableName, qualifiedClassName, columnMap);
        }
    }
}
