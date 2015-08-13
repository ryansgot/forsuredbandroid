package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.FSTable;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/*package*/ class TableInfo {

    private final Map<String, ColumnInfo> columnMap = new HashMap<>();
    private final MetaData metaData;
    private final String qualifiedClassName;
    private final String simpleClassName;
    private final String tableName;

    /*package*/ TableInfo(TypeElement intf) {
        if (intf == null) {
            throw new IllegalStateException("input TypeElement was null");
        }

        metaData = new MetaData(intf.getAnnotationMirrors());
        appendColumns(intf);
        qualifiedClassName = intf.getQualifiedName().toString();
        simpleClassName = intf.getSimpleName().toString();
        tableName = createName();
    }

    @Override
    public String toString() {
        return new StringBuffer("TableInfo {\n\ttableName=").append(tableName)
                                                        .append("\n\tqualifiedClassName=").append(qualifiedClassName)
                                                        .append("\n\tsimpleClassName=").append(simpleClassName)
                                                        .append("\n\tmetaData=").append(metaData.toString())
                                                        .append("\n\tcolumns=").append(columnMap.toString())
                                                        .append("\n}").toString();
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public String getSimpleClassName() {
        return simpleClassName;
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

    public boolean hasAnnotation(Class<? extends Annotation> annotationCls) {
        return metaData.isAnnotationPresent(annotationCls);
    }

    public MetaData.AnnotationTranslator getAnnotation(Class<? extends Annotation> annotationCls) {
        return metaData.get(annotationCls);
    }

    private void appendColumns(TypeElement intf) {
        for (ExecutableElement me : ElementFilter.methodsIn(intf.getEnclosedElements())) {
            ColumnInfo info = ColumnInfo.from(me);
            columnMap.put(info.getColumnName(), info);
        }
    }

    private String createName() {
        if (!metaData.isAnnotationPresent(FSTable.class)) {
            return simpleClassName;
        }

        final String tableName = metaData.get(FSTable.class).property("value").as(String.class);
        return tableName == null || tableName.isEmpty() ? simpleClassName : tableName;
    }
}
