package com.forsuredb;

public interface RecordContainer {
    Object get(String column);
    void put(String column, String value);
    void put(String column, long value);
    void put(String column, int value);
    void put(String column, double value);
    void put(String column, byte[] value);
    void clear();
}
