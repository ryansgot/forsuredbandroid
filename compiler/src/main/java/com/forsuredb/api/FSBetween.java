package com.forsuredb.api;

public interface FSBetween<U, T, F extends FSFilter<U>> {
    FSRecordResolver<U, F> and(T upper);
    FSRecordResolver<U, F> andInclusive(T upper);
}
