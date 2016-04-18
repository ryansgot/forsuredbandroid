/*
   forsuredbandroid, an android companion to the forsuredb project

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

import com.forsuredb.api.FSTableCreator;
import com.forsuredb.cursor.FSCursorFactory;
import com.forsuredb.migration.Migration;
import com.forsuredb.migration.MigrationSet;
import com.forsuredb.sqlite.SqlGenerator;

import java.util.Collections;
import java.util.List;

public class FSDBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = FSDBHelper.class.getSimpleName();
    private static final SQLiteDatabase.CursorFactory cursorFactory = new FSCursorFactory();

    private final List<FSTableCreator> tables;
    private final List<MigrationSet> migrationSets;
    private final Context context;
    private final boolean debugMode;

    private FSDBHelper(Context context, String dbName, List<FSTableCreator> tables, List<MigrationSet> migrationSets, boolean debugMode) {
        super(context, dbName, cursorFactory, identifyDbVersion(migrationSets));
        this.context = context;
        this.tables = tables;
        this.migrationSets = migrationSets;
        this.debugMode = debugMode;
    }

    private static final class Holder {
        public static FSDBHelper instance;
    }

    /**
     * <p>
     *     Call this initializer in onCreate of your {@link android.app.Application} class with
     *     the production version of your app. It has debug mode set to false. If you want
     *     debugMode on, then call {@link #initDebug(Context, String, List)}.
     * </p>
     * @param context The application context
     * @param dbName The name of your database
     * @param tables The information for creating tables
     * @see #initDebug(Context, String, List)
     */
    public static void init(Context context, String dbName, List<FSTableCreator> tables) {
        if (Holder.instance == null) {
            Holder.instance = new FSDBHelper(context, dbName, tables, new Migrator(context).getMigrationSets(), false);
        }
    }

    /**
     * <p>
     *     Call this initializer in onCreate of your {@link android.app.Application} class with
     *     if you want to output all of your queries to logcat with the tag FSCursorFactory.
     *     Otherwise, you can just call {@link #init(Context, String, List)}, which defaults to
     *     debugMode off.
     * </p>
     * @param context The application context
     * @param dbName The name of your database
     * @param tables The information for creating tables
     * @see #init(Context, String, List)
     */
    public static void initDebug(Context context, String dbName, List<FSTableCreator> tables) {
        if (Holder.instance == null) {
            Holder.instance = new FSDBHelper(context, dbName, tables, new Migrator(context).getMigrationSets(), true);
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

    public boolean inDebugMode() {
        return debugMode;
    }

    /**
     * @param migrationSets The {@link List} of {@link Migration}
     * @return either 1 or the largest dbVersion in the migrationSets list
     */
    private static int identifyDbVersion(List<MigrationSet> migrationSets) {
        if (migrationSets == null || migrationSets.size() == 0) {
            return 1;
        }

        int version = 1;
        for (MigrationSet migrationSet : migrationSets) {
            version = migrationSet.getDbVersion() > version ? migrationSet.getDbVersion() : version;
        }
        return version;
    }

    private void performStaticDataInsertion(SQLiteDatabase db, FSTableCreator table) {
        for (String insertionSqlString : new StaticDataSQL(table).getInsertionSQL(context)) {
            db.execSQL(insertionSqlString);
        }
    }

    private void applyMigrations(SQLiteDatabase db, int previousVersion) {
        for (MigrationSet migrationSet : migrationSets) {
            if (previousVersion >= migrationSet.getDbVersion()) {
                continue;
            }

            for (String sql : new SqlGenerator(migrationSet).generate()) {
                db.execSQL(sql);
            }
        }
    }
}
