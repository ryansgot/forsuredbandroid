package com.forsuredb.testapp.model;

import com.forsuredb.api.Retriever;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.ForeignKey;
import com.forsuredb.annotation.FSTable;

@FSTable("profile_info")
public interface ProfileInfoTable extends FSGetApi {
    @FSColumn("user_id") @ForeignKey(apiClass = UserTable.class, columnName = "_id") long userId(Retriever retriever);
    @FSColumn("email_address") String emailAddress(Retriever retriever);
    @FSColumn("binary_data") byte[] binaryData(Retriever retriever);
}
