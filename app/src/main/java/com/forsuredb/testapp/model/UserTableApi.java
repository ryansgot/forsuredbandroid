package com.forsuredb.testapp.model;

import android.database.Cursor;

import com.forsuredb.record.FSColumn;
import com.forsuredb.record.FSApi;
import com.forsuredb.record.PrimaryKey;
import com.forsuredb.table.FSTable;

@FSTable("user")
public interface UserTableApi extends FSApi {
    @FSColumn("_id") @PrimaryKey public long id(Cursor cursor);
    @FSColumn("global_id") public long globalId(Cursor cursor);
}
