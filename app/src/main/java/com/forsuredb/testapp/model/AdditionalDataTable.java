package com.forsuredb.testapp.model;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.FSStaticData;
import com.forsuredb.annotation.FSTable;
import com.forsuredb.annotation.ForeignKey;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.Retriever;

@FSTable("additional_data")
@FSStaticData(asset = "additional_data.xml", recordName = "additional_data")
public interface AdditionalDataTable extends FSGetApi {
    @FSColumn("profile_info_id") @ForeignKey(apiClass = ProfileInfoTable.class, columnName = "_id") long profileInfoId(Retriever retriever);
    @FSColumn("string_column") String stringColumn(Retriever retriever);
    @FSColumn("int_column") int intColumn(Retriever retriever);
    @FSColumn("long_column") long longColumn(Retriever retriever);
}
