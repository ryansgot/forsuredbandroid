package com.forsuredb.api;

public interface Joiner<GP extends FSGetApi, GC extends FSGetApi> {
    GP parentApi();
    GC childApi();
    Retriever join();
}
