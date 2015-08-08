package com.forsuredb;

public interface SaveResult<U> {
    Exception exception();
    U inserted();
    int rowsAffected();
}
