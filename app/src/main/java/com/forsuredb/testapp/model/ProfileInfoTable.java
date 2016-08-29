package com.forsuredb.testapp.model;

import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.ForeignKey;
import com.fsryan.forsuredb.annotations.FSTable;

@FSTable("profile_info")
@FSStaticData(asset = "profile_info.xml", recordName = "profile_info")
public interface ProfileInfoTable extends FSGetApi {

    @FSColumn(value = "user_id", orderable = false, searchable = false)
    @ForeignKey(apiClass = UserTable.class, columnName = "_id")
    long userId(Retriever retriever);

    @FSColumn("email_address")
    String emailAddress(Retriever retriever);

    @FSColumn(value = "binary_data", orderable = false, searchable = false)
    byte[] binaryData(Retriever retriever);

    @FSColumn(value = "awesome", orderable = false, searchable = false)
    boolean awesome(Retriever retriever);
}
