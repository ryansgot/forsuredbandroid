package com.forsuredb;

public interface Retriever {
    String getString(String column);
    int getInt(String column);
    long getLong(String column);
    double getDouble(String column);
    byte[] getBlob(String column);
    int getCount();
    void close();
}
