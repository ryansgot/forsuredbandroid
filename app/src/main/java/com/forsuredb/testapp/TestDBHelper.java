package com.forsuredb.testapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.forsuredb.table.FSTableDescriber;
import com.forsuredb.testapp.model.ProfileInfoTableApi;
import com.forsuredb.testapp.model.UserTableApi;
import com.google.common.collect.Lists;

import java.util.List;

public class TestDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "fs_test.db";
    private static final int DB_VERSION = 1;

    /**
     * Add the tables in the order that is necessary for proper SQL Execution. In other words, if ContactTable
     * has an @FSColumn that is a foreign key reference to an @FSColumn in UserTableDescriber, then UserTable must
     * appear first in this list.
     */
    private static final List<FSTableDescriber> tables = Lists.newArrayList(new FSTableDescriber(TestContentProvider.AUTHORITY, UserTableApi.class, R.xml.user, "user"),
                                                                            new FSTableDescriber(TestContentProvider.AUTHORITY, ProfileInfoTableApi.class, R.xml.profile_info, "profile_info"));

    private final Context context;

    private TestDBHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    private static final class Holder {
        public static TestDBHelper instance;
    }

    public static TestDBHelper getInstance(Context context) {
        if (Holder.instance == null) {
            Holder.instance = new TestDBHelper(context);
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