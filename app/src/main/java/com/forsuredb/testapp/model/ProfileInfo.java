package com.forsuredb.testapp.model;

import com.forsuredb.record.As;
import com.forsuredb.record.FSRecordModel;
import com.forsuredb.record.ForeignKey;
import com.forsuredb.record.PrimaryKey;
import com.forsuredb.table.FSTable;

@FSTable("profile_info")
public class ProfileInfo extends FSRecordModel {

    private ProfileInfo() {}

    private static class Holder {
        public static ProfileInfo model;
    }

    public static ProfileInfo getModel() {
        if (Holder.model == null) {
            Holder.model = new ProfileInfo();
        }
        return Holder.model;
    }

    @PrimaryKey
    @As("_id")
    private Long id;

    @ForeignKey(tableName = "user", columnName = "_id")
    @As("user_id")
    private Long userId;

    @As("email_address")
    private String emailAddress;
}
