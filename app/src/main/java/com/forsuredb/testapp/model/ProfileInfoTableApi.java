package com.forsuredb.testapp.model;

import android.database.Cursor;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.FSApi;
import com.forsuredb.annotation.ForeignKey;
import com.forsuredb.annotation.PrimaryKey;
import com.forsuredb.annotation.FSTable;

@FSTable("profile_info")
public interface ProfileInfoTableApi extends FSApi {
    @FSColumn("_id") @PrimaryKey public long id(Cursor cursor);
    @FSColumn("user_id") @ForeignKey(apiClass = UserTableApi.class, columnName = "_id") public long userId(Cursor cursor);
    @FSColumn("email_address") public String emailAddress(Cursor cursor);
    @FSColumn("binary_data") public byte[] binaryData(Cursor cursor);
}
