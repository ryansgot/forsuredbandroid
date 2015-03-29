package com.forsuredb.testapp.model;

import android.database.Cursor;

import com.forsuredb.record.As;
import com.forsuredb.record.FSApi;
import com.forsuredb.record.ForeignKey;
import com.forsuredb.record.PrimaryKey;

public interface ProfileInfoTableApi extends FSApi {
    @PrimaryKey @As("_id") public long id(Cursor cursor);
    @ForeignKey(tableName = "user", columnName = "_id") @As("user_id") public long userId(Cursor cursor);
    @As("email_address") public String emailAddress(Cursor cursor);
}
