package com.forsuredb.testapp.model;

import com.fsryan.forsuredb.annotations.FSColumn;
import com.fsryan.forsuredb.annotations.FSTable;
import com.fsryan.forsuredb.annotations.Index;
import com.fsryan.forsuredb.annotations.Unique;
import com.fsryan.forsuredb.api.FSDocStoreGetApi;
import com.fsryan.forsuredb.api.Retriever;

@FSTable("doc_store_test")
public interface DocStoreTestTable extends FSDocStoreGetApi<DocStoreTestBase> {
    Class BASE_CLASS = DocStoreTestBase.class;

    @FSColumn(value = "uuid", documentValueAccess = "getUuid")
    @Unique @Index
    String uuid(Retriever retriever);

    @FSColumn(value = "name", documentValueAccess = "getName")
    String name(Retriever retriever);
}
