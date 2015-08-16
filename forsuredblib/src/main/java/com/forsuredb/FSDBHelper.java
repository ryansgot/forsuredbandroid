package com.forsuredb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.forsuredb.migration.Migration;
import com.forsuredb.migrator.Migrator;

import java.util.List;

/*package*/ class FSDBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = FSDBHelper.class.getSimpleName();

    /**
     * Add the tables in the order that is necessary for proper SQL Execution. In other words, if ProfileInfoApi
     * has an @FSColumn that is a foreign key reference to an @FSColumn in UserApi, then UserApi must be used to create an
     * FSTableDescriber first
     */
    private final List<FSTableDescriber> tables;
    private final List<Migration> migrations;
    private final Context context;

    private FSDBHelper(Context context, String dbName, List<FSTableDescriber> tables, List<Migration> migrations) {
        super(context, dbName, null, identifyDbVersion(migrations));
        this.context = context;
        this.tables = tables;
        this.migrations = migrations;
    }

    private static final class Holder {
        public static FSDBHelper instance;
    }

    public static void init(Context context, String dbName, List<FSTableDescriber> tables) {
        if (Holder.instance == null) {
            Holder.instance = new FSDBHelper(context, dbName, tables, new Migrator(context).getMigrations());
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
        for (Migration migration : migrations) {
            Log.i(LOG_TAG, "running migration: " + migration.toString());
            db.execSQL(migration.getQuery());
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
     * @param migrations
     * @return either 1 or the largest dbVersion in the migrations list
     */
    private static int identifyDbVersion(List<Migration> migrations) {
        if (migrations == null || migrations.size() == 0) {
            return 1;
        }

        int version = 1;
        for (Migration migration : migrations) {
            version = migration.getDbVersion() > version ? migration.getDbVersion() : version;
        }
        return version;
    }

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