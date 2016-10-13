package com.forsuredb.testapp.model;

import java.util.Date;

public class DocStoreIntPropertyExtension extends DocStoreTestBase {

    private static final long serialVersionUID = 834743L;

    private int value;

    public DocStoreIntPropertyExtension() {}

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
