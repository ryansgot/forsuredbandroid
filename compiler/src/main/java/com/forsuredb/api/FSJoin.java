package com.forsuredb.api;

public interface FSJoin<U> {

    enum Kind {
        CROSS_JOIN, INNER_JOIN, OUTER_JOIN;

        @Override
        public String toString() {
            return name().replace("_", " ");
        }
    }

    Finder.Operator operator();
    Kind kind();
    String parentTable();
    String parentColumn();
    String childTable();
    String childColumn();
    U parentResource();
    U childResource();
}
