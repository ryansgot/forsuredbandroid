package com.forsuredb;

public interface FSQueryable<U, R extends RecordContainer> {
    U insert(R record);
    int update(R record, FSSelection selection);
    int delete(FSSelection selection);
    Retriever query(FSProjection projection, FSSelection selection, String sortOrder);
}
