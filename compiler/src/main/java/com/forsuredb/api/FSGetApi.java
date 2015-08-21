package com.forsuredb.api;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.PrimaryKey;

public interface FSGetApi {
    @FSColumn("_id") @PrimaryKey long id(Retriever retriever);
}
