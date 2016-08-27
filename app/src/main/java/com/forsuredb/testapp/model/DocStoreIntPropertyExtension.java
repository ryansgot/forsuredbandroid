package com.forsuredb.testapp.model;

import java.util.Date;

public class DocStoreIntPropertyExtension extends DocStoreTestBase {

    private int value;

    public DocStoreIntPropertyExtension(String uuid, String name, Date date, int value) {
        super(uuid, name, date);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
