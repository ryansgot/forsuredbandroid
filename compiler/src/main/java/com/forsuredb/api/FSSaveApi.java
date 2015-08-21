package com.forsuredb.api;

import com.forsuredb.SaveResult;

public interface FSSaveApi<U> {
    SaveResult<U> save();
}
