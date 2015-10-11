package com.forsuredb.api;

public interface FSJoin {
    enum Type {
        NATURAL, LEFT, INNER, OUTER, CROSS;
    }
    Type type();
    String parentTable();
    String parentColumn();
    String childTable();
    String childColumn();
}
