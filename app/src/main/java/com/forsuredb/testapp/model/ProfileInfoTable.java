package com.forsuredb.testapp.model;

import com.fsryan.forsuredb.annotations.FSForeignKey;
import com.fsryan.forsuredb.annotations.FSPrimaryKey;
import com.fsryan.forsuredb.annotations.Unique;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSTable;

@FSTable("profile_info")
@FSStaticData(asset = "profile_info.xml", recordName = "profile_info")
@FSPrimaryKey({"email_address", "uuid"})
public interface ProfileInfoTable extends FSGetApi {

    @FSColumn(value = "user_id", orderable = false, searchable = false)
    @FSForeignKey(
            apiClass = UserTable.class,
            columnName = "_id",
            updateAction = "CASCADE",
            deleteAction = "CASCADE"
    )
    long userId(Retriever retriever);

    @Unique
    @FSColumn("email_address")
    String emailAddress(Retriever retriever);

    @Unique
    @FSColumn("uuid")
    String uuid(Retriever retriever);

    @FSColumn(value = "binary_data", orderable = false, searchable = false)
    byte[] binaryData(Retriever retriever);

    @FSColumn("awesome")
    boolean awesome(Retriever retriever);
}
