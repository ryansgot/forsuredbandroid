/*
   forsuredb, an object relational mapping tool

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.forsuredb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.forsuredb.api.FSTableCreator;
import com.forsuredb.migration.Migration;

import java.util.Collections;
import java.util.List;

/*package*/ class FSDBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = FSDBHelper.class.getSimpleName();

    private final List<FSTableCreator> tables;
    private final List<Migration> migrations;
    private final Context context;

    private FSDBHelper(Context context, String dbName, List<FSTableCreator> tables, List<Migration> migrations) {
        super(context, dbName, null, identifyDbVersion(migrations));
        this.context = context;
        this.tables = tables;
        this.migrations = migrations;
    }

    private static final class Holder {
        public static FSDBHelper instance;
    }

    public static void init(Context context, String dbName, List<FSTableCreator> tables) {
        if (Holder.instance == null) {
            Holder.instance = new FSDBHelper(context, dbName, tables, new Migrator(context).getMigrations());
        }
    }

    public static FSDBHelper inst() {
        if (Holder.instance == null) {
            throw new IllegalStateException("Must call FSDBHelper.init prior to getting instance");
        }
        return Holder.instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        applyMigrations(db, 0);

        Collections.sort(tables);
        for (FSTableCreator table : tables) {
            performStaticDataInsertion(db, table);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        applyMigrations(db, oldVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /**
     * @param migrations The {@link List} of {@link Migration}
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

    private void performStaticDataInsertion(SQLiteDatabase db, FSTableCreator table) {
        for (String insertionSqlString : new StaticDataSQL(table).getInsertionSQL(context)) {
            db.execSQL(insertionSqlString);
        }
    }

    private void applyMigrations(SQLiteDatabase db, int previousVersion) {
        for (Migration migration : migrations) {
            if (previousVersion < migration.getDbVersion()) {
                Log.i(LOG_TAG, "running migration: " + migration.toString());
                db.execSQL(migration.getQuery());
            }
        }
    }
}