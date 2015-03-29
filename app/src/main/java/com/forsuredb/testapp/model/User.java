package com.forsuredb.testapp.model;

import com.forsuredb.record.As;
import com.forsuredb.record.FSRecordModel;
import com.forsuredb.record.PrimaryKey;
import com.forsuredb.table.FSTable;

@FSTable("user")
public class User extends FSRecordModel {

    private User() {}

    private static class Holder {
        public static User model;
    }

    public static User getModel() {
        if (Holder.model == null) {
            Holder.model = new User();
        }
        return Holder.model;
    }

    @PrimaryKey
    @As("_id")
    private Long id;

    @As("global_id")
    private Long globalId;
}
