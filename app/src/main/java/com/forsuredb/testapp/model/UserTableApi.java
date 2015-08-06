package com.forsuredb.testapp.model;

import android.database.Cursor;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.FSApi;
import com.forsuredb.annotation.PrimaryKey;
import com.forsuredb.annotation.FSTable;

import java.math.BigDecimal;

@FSTable("user")
public interface UserTableApi extends FSApi {
    @FSColumn("_id") @PrimaryKey public long id(Cursor cursor);
    @FSColumn("global_id") public long globalId(Cursor cursor);
    @FSColumn("login_count") public int loginCount(Cursor cursor);
    @FSColumn("app_rating") public double appRating(Cursor cursor);
    @FSColumn("competitor_app_rating") public BigDecimal competitorAppRating(Cursor cursor);
}
