package com.forsuredb.migration;

public class Migration {

    private final String tableName;
    private final int dbVersion;
    private final String query;
    private final QueryGenerator.MigrationType migrationType;
    private final String columnName;
    private final String columnQualifiedType;
    private final String foreignKeyTable;
    private final String foreignKeyColumn;
    private final boolean isLastInSet;

    private Migration(int dbVersion,
                      String tableName,
                      String query,
                      QueryGenerator.MigrationType migrationType,
                      String columnName,
                      String columnQualifiedType,
                      String foreignKeyTable,
                      String foreignKeyColumn,
                      boolean isLastInSet) {
        this.dbVersion = dbVersion;
        this.tableName = tableName;
        this.query = query;
        this.migrationType = migrationType;
        this.columnName = columnName;
        this.columnQualifiedType = columnQualifiedType;
        this.foreignKeyTable = foreignKeyTable;
        this.foreignKeyColumn = foreignKeyColumn;
        this.isLastInSet = isLastInSet;
    }

    @Override
    public String toString() {
        return new StringBuffer(Migration.class.getSimpleName()).append("{dbVerison=").append(dbVersion)
                                                                .append(", tableName=").append(tableName)
                                                                .append(", migrationType=").append(migrationType == null ? "null" : migrationType.name())
                                                                .append(", columnName=").append(columnName)
                                                                .append(", columnQualifiedType=").append(columnQualifiedType)
                                                                .append(", foreignKeyTable=").append(foreignKeyTable)
                                                                .append(", foreignKeyColumn=").append(foreignKeyColumn)
                                                                .append(", query=").append(query)
                                                                .append(", isLastInSet=").append(isLastInSet)
                                                                .append("}").toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTableName() {
        return tableName;
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public String getQuery() {
        return query;
    }

    public QueryGenerator.MigrationType getMigrationType() {
        return migrationType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnQualifiedType() {
        return columnQualifiedType;
    }

    public String getForeignKeyTable() {
        return foreignKeyTable;
    }

    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }

    public boolean isLastInSet() {
        return isLastInSet;
    }

    public static class Builder {

        private String tableName;
        private int dbVersion = 1;
        private String query;
        private QueryGenerator.MigrationType migrationType;
        private String columnName;
        private String columnQualifiedType;
        private String foreignKeyTable;
        private String foreignKeyColumn;
        private boolean isLastInSet;

        private Builder() {}

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder dbVersion(int dbVersion) {
            this.dbVersion = dbVersion > 0 ? dbVersion : this.dbVersion;
            return this;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder migrationType(QueryGenerator.MigrationType migrationType) {
            this.migrationType = migrationType;
            return this;
        }

        public Builder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public Builder columnQualifiedType(String columnQualifiedType) {
            this.columnQualifiedType = columnQualifiedType;
            return this;
        }

        public Builder foreignKeyTable(String foreignKeyTable) {
            this.foreignKeyTable = foreignKeyTable;
            return this;
        }

        public Builder foreignKeyColumn(String foreignKeyColumn) {
            this.foreignKeyColumn = foreignKeyColumn;
            return this;
        }

        public Builder isLastInSet(Boolean isLastInSet) {
            this.isLastInSet = isLastInSet;
            return this;
        }

        public Migration build() {
            if (!canBuild()) {
                throw new IllegalStateException("Cannot build migration with null or empty table name or query");
            }
            return new Migration(dbVersion, tableName, query, migrationType, columnName, columnQualifiedType, foreignKeyTable, foreignKeyColumn, isLastInSet);
        }

        private boolean canBuild() {
            return tableName != null && !tableName.isEmpty() && query != null && !query.isEmpty();
        }
    }
}