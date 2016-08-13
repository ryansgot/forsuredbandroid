package com.forsuredb.testapp.model;

import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSTable;

import java.math.BigDecimal;

@FSTable("user")
@FSStaticData(asset = "user.xml", recordName = "user")
public interface UserTable extends FSGetApi {
    @FSColumn("global_id") long globalId(Retriever retriever);
    @FSColumn("login_count") int loginCount(Retriever retriever);
    @FSColumn("app_rating") double appRating(Retriever retriever);
    @FSColumn("competitor_app_rating") BigDecimal competitorAppRating(Retriever retriever);
}
