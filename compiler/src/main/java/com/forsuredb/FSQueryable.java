package com.forsuredb;

public interface FSQueryable<U, R, C> {
    U insert(R record);
    int update(R record, FSSelection selection);
    int delete(FSSelection selection);
    C query(FSProjection projection, FSSelection selection, String sortOrder);
}
