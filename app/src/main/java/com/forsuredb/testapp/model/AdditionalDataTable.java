package com.forsuredb.testapp.model;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.annotations.ForeignKey;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;

@FSTable("additional_data")
@FSStaticData(asset = "additional_data.xml", recordName = "additional_data")
public interface AdditionalDataTable extends FSGetApi {
    @FSColumn("profile_info_id") @ForeignKey(apiClass = ProfileInfoTable.class, columnName = "_id") long profileInfoId(Retriever retriever);
    @FSColumn("string_column") String stringColumn(Retriever retriever);
    @FSColumn("int_column") int intColumn(Retriever retriever);
    @FSColumn("long_column") long longColumn(Retriever retriever);
}
