package com.forsuredb.testapp.model;

import android.database.Cursor;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.FSGetApi;
import com.forsuredb.annotation.ForeignKey;
import com.forsuredb.annotation.PrimaryKey;
import com.forsuredb.annotation.FSTable;

@FSTable("profile_info")
public interface ProfileInfoTable extends FSGetApi {
    @FSColumn("_id") @PrimaryKey long id(Cursor cursor);
    @FSColumn("user_id") @ForeignKey(apiClass = UserTable.class, columnName = "_id") long userId(Cursor cursor);
    @FSColumn("email_address") String emailAddress(Cursor cursor);
    @FSColumn("binary_data") byte[] binaryData(Cursor cursor);
}
