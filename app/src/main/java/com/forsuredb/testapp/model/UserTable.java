package com.forsuredb.testapp.model;

import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSTable;

import java.math.BigDecimal;

@FSTable("user")
@FSStaticData("user.xml")
public interface UserTable extends FSGetApi {
    @FSColumn(value = "global_id", orderable = false, searchable = false)
    long globalId(Retriever retriever);

    @FSColumn("login_count")
    int loginCount(Retriever retriever);

    @FSColumn(value = "app_rating", orderable = false, searchable = false)
    double appRating(Retriever retriever);

    @FSColumn(value = "competitor_app_rating", orderable = false, searchable = false)
    BigDecimal competitorAppRating(Retriever retriever);
}
