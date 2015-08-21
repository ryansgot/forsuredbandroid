package com.forsuredb.api;

public interface SaveResult<U> {
    Exception exception();
    U inserted();
    int rowsAffected();
}
