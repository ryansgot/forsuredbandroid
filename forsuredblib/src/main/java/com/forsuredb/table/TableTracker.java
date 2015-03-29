package com.forsuredb.table;

import java.util.HashMap;
import java.util.Map;

public class TableTracker {

    private Map<String, FSTableDescriber> tableDescriberMap = new HashMap<String, FSTableDescriber>();

    private TableTracker() {}

    private static class Holder {
        public static TableTracker instance;
    }

    public static TableTracker getInstance() {
        if (Holder.instance == null) {
            Holder.instance = new TableTracker();
        }
        return Holder.instance;
    }

    public void put(FSTableDescriber fsTableDescriber) {
        tableDescriberMap.put(fsTableDescriber.getName(), fsTableDescriber);
    }

    public FSTableDescriber get(String tableName) {
        return tableName == null ? null : tableDescriberMap.get(tableName);
    }

    public boolean containsTable(String tableName) {
        return tableDescriberMap.containsKey(tableName);
    }
}
