package com.forsuredb.testapp.model;

import android.database.Cursor;

import com.forsuredb.record.As;
import com.forsuredb.record.FSApi;
import com.forsuredb.record.PrimaryKey;
import com.forsuredb.table.FSTable;

@FSTable("user")
public interface UserTableApi extends FSApi {
    @PrimaryKey @As("_id") public long id(Cursor cursor);
    @As("global_id") public long globalId(Cursor cursor);
}
