package com.forsuredb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

/*package*/ class FSDBHelper extends SQLiteOpenHelper {

    /**
     * Add the tables in the order that is necessary for proper SQL Execution. In other words, if ProfileInfoApi
     * has an @FSColumn that is a foreign key reference to an @FSColumn in UserApi, then UserApi must be used to create an
     * FSTableDescriber first
     */
    private final List<FSTableDescriber> tables;

    private final Context context;

    private FSDBHelper(Context context, String dbName, int dbVersion, List<FSTableDescriber> tables) {
        super(context, dbName, null, dbVersion);
        this.context = context;
        this.tables = tables;
    }

    private static final class Holder {
        public static FSDBHelper instance;
    }

    public static void init(Context context, String dbName, int dbVersion, List<FSTableDescriber> tables) {
        if (Holder.instance == null) {
            Holder.instance = new FSDBHelper(context, dbName, dbVersion, tables);
        }
    }

    public static FSDBHelper getInstance() {
        if (Holder.instance == null) {
            throw new IllegalStateException("Must call FSDBHelper.init prior to getting instance");
        }
        return Holder.instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        for (FSTableDescriber table : tables) {
            db.execSQL(table.getTableCreateQuery());
        }
        // All tables must be created before inserting any data
        for (FSTableDescriber table : tables) {
            performStaticDataInsertion(db, table);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // traverse the tables list in reverse order to drop them all without blowing up
        for (int i = tables.size() - 1; i >= 0; i--) {
            db.execSQL("DROP TABLE IF EXISTS " + tables.get(i).getName());
        }
        onCreate(db);
    }

    // Private methods

    /**
     * <p>
     *     Insert the static data from the table into the db
     * </p>
     *
     * @param db
     * @param table
     */
    private void performStaticDataInsertion(SQLiteDatabase db, FSTableDescriber table) {
        final List<String> insertionSqlStringList = table.getStaticInsertsSQL(context);
        if (insertionSqlStringList != null && insertionSqlStringList.size() != 0) {
            for (String insertionSqlString : insertionSqlStringList) {
                db.execSQL(insertionSqlString);
            }
        }
    }
}