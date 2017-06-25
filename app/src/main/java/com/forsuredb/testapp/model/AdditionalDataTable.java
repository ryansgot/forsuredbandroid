package com.forsuredb.testapp.model;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSForeignKey;
import com.fsryan.forsuredb.annotations.FSStaticData;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.Retriever;

@FSTable("additional_data")
@FSStaticData(asset = "additional_data.xml", recordName = "additional_data")
public interface AdditionalDataTable extends FSGetApi {

    @FSColumn(value = "email", orderable = false, searchable = false)
    @FSForeignKey(
            compositeId = "profile_info_table", // <-- matching composite id for composite foreign key
            apiClass = ProfileInfoTable.class,
            columnName = "email_address",
            updateAction = "CASCADE",
            deleteAction = "CASCADE"
    )
    String emailAddress(Retriever retriever);

    @FSColumn(value = "profile_info_uuid", orderable = false, searchable = false)
    @FSForeignKey(
            compositeId = "profile_info_table", // <-- matching composite id for composite foreign key
            apiClass = ProfileInfoTable.class,
            columnName = "uuid",
            updateAction = "CASCADE",
            deleteAction = "CASCADE"
    )
    String uuid(Retriever retriever);

    @FSColumn(value = "string_column", orderable = false, searchable = false)
    String stringColumn(Retriever retriever);

    @FSColumn(value = "int_column", orderable = false, searchable = false)
    int intColumn(Retriever retriever);

    @FSColumn(value = "long_column", orderable = false, searchable = false)
    long longColumn(Retriever retriever);
}
