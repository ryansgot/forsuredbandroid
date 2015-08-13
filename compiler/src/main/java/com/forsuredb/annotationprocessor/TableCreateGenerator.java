package com.forsuredb.annotationprocessor;

import com.forsuredb.annotation.ForeignKey;
import com.forsuredb.annotation.PrimaryKey;

import java.util.List;

/*package*/ class TableCreateGenerator extends XmlGenerator {

    private final TableInfo tableInfo;

    /*package*/ TableCreateGenerator(int dbVersion, TableInfo tableInfo, Keyword keyword) {
        super(dbVersion, tableInfo.getTableName(), keyword);
        this.tableInfo = tableInfo;
    }

    @Override
    protected String generateQuery(DBType dbType, List<TableInfo> allTables) {
        StringBuffer queryBuffer = new StringBuffer("CREATE TABLE ").append(tableInfo.getTableName()).append("(");

        for (ColumnInfo columnInfo : tableInfo.getColumns()) {
            appendColumnDefinitionTo(queryBuffer, dbType, columnInfo);
            queryBuffer.append(", ");
        }
        queryBuffer.delete(queryBuffer.length() - 2, queryBuffer.length()); // <-- remove final ", "
        appendForeignKeysLineTo(queryBuffer, dbType, allTables);
        return queryBuffer.append(");").toString();
    }

    private void appendColumnDefinitionTo(StringBuffer queryBuffer, DBType dbType, ColumnInfo columnInfo) {
        queryBuffer.append(columnInfo.getColumnName()).append(" ")
                .append(typeString(dbType, columnInfo));

        String primaryKeyDefinition = primaryKeyDefinition(dbType, columnInfo);
        if (primaryKeyDefinition.isEmpty()) {
            return;
        }
        queryBuffer.append(" ").append(primaryKeyDefinition);
    }

    private String primaryKeyDefinition(DBType dbType, ColumnInfo columnInfo) {
        if (!columnInfo.isAnnotationPresent(PrimaryKey.class)) {
            return "";
        }

        switch (dbType) {
            case SQLITE:
                return ColumnDescriptor.PRIMARY_KEY.getSqliteDefinition();
        }

        return "";
    }

    private String typeString(DBType dbType, ColumnInfo columnInfo) {
        final TypeTranslator typeTranslator = TypeTranslator.getFrom(columnInfo.getType());
        switch (dbType) {
            case SQLITE:
                return typeTranslator.getSQLiteTypeString();
        }

        return typeTranslator.getTypeMirrorString();
    }

    // TODO: use DBType parameter
    private void appendForeignKeysLineTo(StringBuffer queryBuffer, DBType dbType, List<TableInfo> allTables) {
        for (ColumnInfo columnInfo : tableInfo.getColumns()) {
            if (!columnInfo.isAnnotationPresent(ForeignKey.class)) {
                continue;   // <-- only operate on foreign keys
            }

            final MetaData.AnnotationTranslator aTranslator = columnInfo.getAnnotation(ForeignKey.class);
            final String fKeyTable = getForeignTableName(aTranslator.property("apiClass").uncasted(), allTables);
            final String fKeyColumn = aTranslator.property("columnName").as(String.class);
            queryBuffer.append(", FOREIGN KEY(")
                    .append(columnInfo.getColumnName())
                    .append(") REFERENCES ")
                    .append(fKeyTable)
                    .append("(").append(fKeyColumn)
                    .append(")");
        }
    }

    private String getForeignTableName(Object uncastedClassObject, List<TableInfo> allTables) {
        for (TableInfo table : allTables) {
            if (table.getQualifiedClassName().equals(uncastedClassObject.toString())) {
                return table.getTableName();
            }
        }

        return "";
    }
}
