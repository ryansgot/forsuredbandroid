package com.forsuredb;

import java.util.List;

public abstract class QueryGenerator implements Comparable<QueryGenerator> {

    private final MigrationType type;
    private final String tableName;

    public QueryGenerator(String tableName, MigrationType type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("tableName cannot be null or empty");
        }
        this.type = type;
        this.tableName = tableName;
    }

    @Override
    public int compareTo(QueryGenerator other) {
        if (other == null) {
            return -1;
        }
        return type.getPriority() - other.getMigrationType().getPriority();
    }

    public abstract List<String> generate();

    public MigrationType getMigrationType() {
        return type;
    }

    public String getTableName() {
        return tableName;
    }

    public enum MigrationType {
        CREATE_TABLE(0),
        ALTER_TABLE_ADD_COLUMN(1),
        ADD_FOREIGN_KEY_REFERENCE(2);

        private int priority;

        MigrationType(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }
}
