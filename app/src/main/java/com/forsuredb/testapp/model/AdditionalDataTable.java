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

    @FSColumn(value = "profile_info_id", orderable = false, searchable = false)
    @ForeignKey(apiClass = ProfileInfoTable.class, columnName = "_id")
    long profileInfoId(Retriever retriever);

    @FSColumn(value = "string_column", orderable = false, searchable = false)
    String stringColumn(Retriever retriever);

    @FSColumn(value = "int_column", orderable = false, searchable = false)
    int intColumn(Retriever retriever);

    @FSColumn(value = "long_column", orderable = false, searchable = false)
    long longColumn(Retriever retriever);
}
