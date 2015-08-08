package com.forsuredb;

import java.util.List;

public interface SaveResult {
    List<Error> errors();
    int rowsAffected();
}
