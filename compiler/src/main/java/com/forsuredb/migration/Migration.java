package com.forsuredb.migration;

public class Migration {

    private final String tableName;
    private final int dbVersion;
    private final String query;

    private Migration(int dbVersion, String tableName, String query) {
        this.dbVersion = dbVersion;
        this.tableName = tableName;
        this.query = query;
    }

    @Override
    public String toString() {
        return new StringBuffer(Migration.class.getSimpleName()).append("{dbVerison=").append(dbVersion)
                                                                .append(", tableName=").append(tableName)
                                                                .append(", query=").append(query).append("}")
                                                                .toString();
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

    public static class Builder {

        private String tableName;
        private int dbVersion = 1;
        private String query;

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

        public Migration build() {
            if (!canBuild()) {
                throw new IllegalStateException("Cannot build migration with null or empty table name or query");
            }
            return new Migration(dbVersion, tableName, query);
        }

        private boolean canBuild() {
            return tableName != null && !tableName.isEmpty() && query != null && !query.isEmpty();
        }
    }
}
