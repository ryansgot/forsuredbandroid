package com.forsuredb.testapp.model;

import android.database.Cursor;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.FSGetApi;
import com.forsuredb.annotation.PrimaryKey;
import com.forsuredb.annotation.FSTable;

import java.math.BigDecimal;

@FSTable("user")
public interface UserTable extends FSGetApi {
    @FSColumn("_id") @PrimaryKey long id(Cursor cursor);
    @FSColumn("global_id") long globalId(Cursor cursor);
    @FSColumn("login_count") int loginCount(Cursor cursor);
    @FSColumn("app_rating") double appRating(Cursor cursor);
    @FSColumn("competitor_app_rating") BigDecimal competitorAppRating(Cursor cursor);
}
