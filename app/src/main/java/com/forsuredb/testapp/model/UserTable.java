package com.forsuredb.testapp.model;

import com.forsuredb.Retriever;
import com.forsuredb.annotation.FSColumn;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.annotation.PrimaryKey;
import com.forsuredb.annotation.FSTable;

import java.math.BigDecimal;

@FSTable("user")
public interface UserTable extends FSGetApi {
    @FSColumn("_id") @PrimaryKey long id(Retriever retriever);
    @FSColumn("global_id") long globalId(Retriever retriever);
    @FSColumn("login_count") int loginCount(Retriever retriever);
    @FSColumn("app_rating") double appRating(Retriever retriever);
    @FSColumn("competitor_app_rating") BigDecimal competitorAppRating(Retriever retriever);
}
