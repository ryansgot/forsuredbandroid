package com.forsuredb.testapp.model;

import java.util.Date;

public class DocStoreDoublePropertyExtension extends DocStoreTestBase {

    private static final long serialVersionUID = 6583865L;

    private double value;

    public DocStoreDoublePropertyExtension() {}

    public DocStoreDoublePropertyExtension(String uuid, String name, Date date, double value) {
        super(uuid, name, date);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
