package com.forsuredb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.forsuredb.record.FSApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForSure {

    private final Map<Class<? extends FSApi>, FSTableDescriber> tableDescriberByApi = new HashMap<Class<? extends FSApi>, FSTableDescriber>();
    private final Map<String, FSTableDescriber> tableDescriberByName = new HashMap<String, FSTableDescriber>();

    private ForSure() {}

    private static class Holder {
        public static ForSure instance;
    }

    /**
     * <p>
     *     Initializes the underlying database tables and
     * </p>
     * @param context
     * @param tableCreators
     */
    public static void init(Context context, String dbName, int dbVersion, List<FSTableCreator> tableCreators) {
        if (Holder.instance == null) {
            Holder.instance = new ForSure();
            FSDBHelper.init(context.getApplicationContext(), dbName, dbVersion, initializeTableDescribers(tableCreators));
        }
    }

    public static ForSure getInstance() throws IllegalStateException {
        if (Holder.instance == null) {
            throw new IllegalStateException("Must call ForSure.init method prior to getting an instance");
        }
        return Holder.instance;
    }

    private static List<FSTableDescriber> initializeTableDescribers(List<FSTableCreator> tableCreators) {
        final List<FSTableDescriber> retList = new ArrayList<FSTableDescriber>();
        for (FSTableCreator tableCreator : tableCreators) {
            final FSTableDescriber table = new FSTableDescriber(tableCreator);
            Holder.instance.addTable(table);
            retList.add(table);
        }
        return retList;
    }

    public SQLiteDatabase getReadableDatabase() {
        return FSDBHelper.getInstance().getReadableDatabase();
    }

    public SQLiteDatabase getWritableDatabase() {
        return FSDBHelper.getInstance().getWritableDatabase();
    }

    public <T> T getTableApi(Class<T> tableApiClass) {
        return tableApiClass == null ? null : (T) tableDescriberByApi.get(tableApiClass).getTableApi();
    }

    public FSTableDescriber getTable(String tableName) {
        return tableName == null ? null : tableDescriberByName.get(tableName);
    }

    public boolean containsTable(String tableName) {
        return tableDescriberByName.containsKey(tableName);
    }

    private void addTable(FSTableDescriber fsTableDescriber) {
        tableDescriberByApi.put(fsTableDescriber.getTableApiClass(), fsTableDescriber);
        tableDescriberByName.put(fsTableDescriber.getName(), fsTableDescriber);
    }
}
