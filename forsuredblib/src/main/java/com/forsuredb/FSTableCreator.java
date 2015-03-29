package com.forsuredb;

import com.forsuredb.record.FSApi;

public class FSTableCreator {

    private final String authority;
    private final Class<? extends FSApi> tableApiClass;
    private final int staticDataResId;
    private final String staticDataRecordName;

    public FSTableCreator(String authority, Class<? extends FSApi> tableApiClass, int staticDataResId, String staticDataRecordName) {
        this.authority = authority;
        this.tableApiClass = tableApiClass;
        this.staticDataResId = staticDataResId;
        this.staticDataRecordName = staticDataRecordName;
    }

    public FSTableCreator(String authority, Class<? extends FSApi> tableApiClass) {
        this(authority, tableApiClass, FSTableDescriber.NO_STATIC_DATA_RESOURCE_ID, "");
    }

    public String getAuthority() {
        return authority;
    }

    public Class<? extends FSApi> getTableApiClass() {
        return tableApiClass;
    }

    public int getStaticDataResId() {
        return staticDataResId;
    }

    public String getStaticDataRecordName() {
        return staticDataRecordName;
    }
}
