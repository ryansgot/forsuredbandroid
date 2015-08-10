package com.forsuredb;

import java.util.Date;

public abstract class Migration implements Comparable<Migration> {

    private final Date date;
    private final String tableName;

    public Migration(String tableName) {
        this(tableName, null);
    }

    public Migration(String tableName, Date date) {
        this.tableName = tableName;
        this.date = date == null ? new Date() : date;
    }

    @Override
    public int compareTo(Migration o) {
        if (o == null || o.getDate() == null) {
            return 1;
        }
        return date.compareTo(o.getDate());
    }

    public String getTableName() {
        return tableName;
    }

    public Date getDate() {
        return date;
    }

    public abstract String getTableCreateQuery();
}
