package com.forsuredb.testapp.model;

import android.database.Cursor;

import com.forsuredb.record.FSColumn;
import com.forsuredb.record.FSApi;
import com.forsuredb.record.ForeignKey;
import com.forsuredb.record.PrimaryKey;
import com.forsuredb.FSTable;

@FSTable("profile_info")
public interface ProfileInfoTableApi extends FSApi {
    @FSColumn("_id") @PrimaryKey public long id(Cursor cursor);
    @FSColumn("user_id") @ForeignKey(apiClass = UserTableApi.class, columnName = "_id") public long userId(Cursor cursor);
    @FSColumn("email_address") public String emailAddress(Cursor cursor);
    @FSColumn("binary_data") public byte[] binaryData(Cursor cursor);
}
