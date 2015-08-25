package com.forsuredb.api;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.PrimaryKey;

import java.util.Date;

public interface FSGetApi {
    @FSColumn("_id") @PrimaryKey long id(Retriever retriever);
    @FSColumn("created") Date created(Retriever retriever);
    @FSColumn("modified") Date modified(Retriever retriever);
    @FSColumn("deleted") boolean deleted(Retriever retriever);
}
