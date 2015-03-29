package com.forsuredb.table;

import java.util.HashMap;
import java.util.Map;

public class ForSure {

    private Map<String, FSTableDescriber> tableDescriberMap = new HashMap<String, FSTableDescriber>();

    private ForSure() {}

    private static class Holder {
        public static ForSure instance;
    }

    public static ForSure getInstance() {
        if (Holder.instance == null) {
            Holder.instance = new ForSure();
        }
        return Holder.instance;
    }

    public void putTable(FSTableDescriber fsTableDescriber) {
        tableDescriberMap.put(fsTableDescriber.getName(), fsTableDescriber);
    }

    public FSTableDescriber getTable(String tableName) {
        return tableName == null ? null : tableDescriberMap.get(tableName);
    }

    public boolean containsTable(String tableName) {
        return tableDescriberMap.containsKey(tableName);
    }
}
