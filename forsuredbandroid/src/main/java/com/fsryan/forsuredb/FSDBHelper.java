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
package com.fsryan.forsuredb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.fsryan.forsuredb.api.FSTableCreator;
import com.fsryan.forsuredb.api.RecordContainer;
import com.fsryan.forsuredb.api.TableInfoUtil;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;
import com.fsryan.forsuredb.api.staticdata.OnRecordRetrievedListener;
import com.fsryan.forsuredb.api.staticdata.StaticDataRetrieverFactory;
import com.fsryan.forsuredb.cursor.FSCursorFactory;
import com.fsryan.forsuredb.info.TableInfo;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fsryan.forsuredb.StatementBinder.bindObjects;

public class FSDBHelper extends SQLiteOpenHelper {

    private static final SQLiteDatabase.CursorFactory cursorFactory = new FSCursorFactory();

    private final List<FSTableCreator> tables;
    private final List<MigrationSet> migrationSets;
    private final Context context;
    private final FSDbInfoSerializer dbInfoSerializer;
    private final boolean debugMode;

    private FSDBHelper(Context context,
                       String dbName,
                       List<FSTableCreator> tables,
                       List<MigrationSet> migrationSets,
                       FSDbInfoSerializer dbInfoSerializer,
                       boolean debugMode) {
        super(context, dbName, cursorFactory, identifyDbVersion(migrationSets));
        this.context = context;
        this.tables = tables;
        Collections.sort(this.tables);
        this.migrationSets = migrationSets;
        this.dbInfoSerializer = dbInfoSerializer;
        this.debugMode = debugMode;
    }

    private static final class Holder {
        public static FSDBHelper instance;
    }

    /**
     * <p>
     *     Call this initializer in onCreate of your {@link android.app.Application} class with
     *     the production version of your app. It has debug mode set to false. If you want
     *     debugMode on, then call {@link #initDebug(Context, String, List, FSDbInfoSerializer)}.
     * </p>
     * @param context The application context
     * @param dbName The name of your database
     * @param tables The information for creating tables
     * @see #initDebug(Context, String, List, FSDbInfoSerializer)
     */
    public static synchronized void init(Context context,
                                         String dbName,
                                         List<FSTableCreator> tables,
                                         FSDbInfoSerializer dbInfoSerializer) {
        if (Holder.instance == null) {
            List<MigrationSet> migrationSets = new Migrator(context, dbInfoSerializer).getMigrationSets();
            Holder.instance = new FSDBHelper(context, dbName, tables, migrationSets, dbInfoSerializer, false);
        }
    }

    /**
     * <p>
     *     Call this initializer in onCreate of your {@link android.app.Application} class with
     *     if you want to output all of your queries to logcat with the tag FSCursorFactory.
     *     Otherwise, you can just call {@link #init(Context, String, List, FSDbInfoSerializer)}, which defaults to
     *     debugMode off.
     * </p>
     * @param context The application context
     * @param dbName The name of your database
     * @param tables The information for creating tables
     * @see #init(Context, String, List, FSDbInfoSerializer)
     */
    public static synchronized void initDebug(Context context,
                                              String dbName,
                                              List<FSTableCreator> tables,
                                              FSDbInfoSerializer dbInfoSerializer) {
        if (Holder.instance == null) {
            List<MigrationSet> migrationSets = new Migrator(context, dbInfoSerializer).getMigrationSets();
            Holder.instance = new FSDBHelper(context, dbName, tables, migrationSets, dbInfoSerializer, true);
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
     * @param migrationSets The {@link List} of
     * {@link com.fsryan.forsuredb.migration.MigrationSet MigrationSet}
     * @return either 1 or the largest dbVersion in the migrationSets list
     */
    private static int identifyDbVersion(List<MigrationSet> migrationSets) {
        if (migrationSets == null || migrationSets.size() == 0) {
            return 1;
        }

        int version = 1;
        for (MigrationSet migrationSet : migrationSets) {
            version = migrationSet.dbVersion() > version ? migrationSet.dbVersion() : version;
        }
        return version;
    }

    private void applyMigrations(SQLiteDatabase db, int previousVersion) {
        int staticDataInsertFromVersion = 0;
        final Map<String, Map<Integer, List<RecordContainer>>> versionToStaticDataRecordContainers = new HashMap<>();
        while (migrationSets.size() > 0) {
            MigrationSet migrationSet = migrationSets.get(0);
            int version = migrationSet.dbVersion();
            if (previousVersion >= version) {
                migrationSets.remove(0);
                continue;
            }

            if (staticDataInsertFromVersion == 0) {
                staticDataInsertFromVersion = migrationSet.dbVersion();
                versionToStaticDataRecordContainers.putAll(createStaticDataRecordContainers());
            }
            migrationSets.remove(0);

            final List<String> sqlScript = Sql.generator().generateMigrationSql(migrationSet, dbInfoSerializer);
            migrateSchema(db, sqlScript, "performing migration sql: ");
            insertStaticData(db, migrationSet, versionToStaticDataRecordContainers);
        }
    }
    private void insertStaticData(SQLiteDatabase db, MigrationSet migrationSet, Map<String, Map<Integer, List<RecordContainer>>> versionToStaticDataRecordContainers) {
        // TODO: use map instead of list to store TableCreators
        for (TableInfo table : TableInfoUtil.bestEffortDAGSort(migrationSet.targetSchema())) {
            if (!hasStaticData(table.tableName())) {
                continue;
            }

            Map<Integer, List<RecordContainer>> versionRecordMap = versionToStaticDataRecordContainers.get(table.tableName());
            if (versionRecordMap == null) {
                continue;
            }

            List<RecordContainer> records = versionRecordMap.get(migrationSet.dbVersion());
            if (records == null) {
                continue;
            }
            insertStaticData(db, table.tableName(), records);
        }
    }

    private boolean hasStaticData(String tableName) {
        for (FSTableCreator tc : tables) {
            if (!tc.getTableName().equals(tableName)) {
                continue;
            }
            return tc.getStaticDataAsset() != null && !tc.getStaticDataAsset().isEmpty();
        }
        return false;
    }

    private void migrateSchema(SQLiteDatabase db, List<String> sqlScript, String logPrefix) {
        // TODO: use preparedstatements
        if (debugMode) {
            for (String insertionSqlString : sqlScript) {
                Log.d("forsuredb", logPrefix + insertionSqlString);
                db.execSQL(insertionSqlString);
            }
        } else {
            for (String insertionSqlString : sqlScript) {
                db.execSQL(insertionSqlString);
            }
        }
    }

    private Map<String, Map<Integer, List<RecordContainer>>> createStaticDataRecordContainers() {
        final Map<String, Map<Integer, List<RecordContainer>>> ret = new HashMap<>();
        for (final FSTableCreator tc : tables) {
            String staticDataAsset = tc.getStaticDataAsset();
            if (staticDataAsset == null || staticDataAsset.isEmpty()) {
                continue;
            }

            InputStream xmlStream = null;
            try {
                xmlStream = context.getAssets().open(staticDataAsset);
                StaticDataRetrieverFactory.createFor(tc.getTableName(), migrationSets, xmlStream)
                        .retrieve(new OnRecordRetrievedListener() {
                    @Override
                    public void onRecord(Map<Integer, List<RecordContainer>> versionRecordMap) {
                        ret.put(tc.getTableName(), versionRecordMap);
                    }
                });
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                if (xmlStream != null) {
                    try {
                        xmlStream.close();
                    } catch (IOException e) {
                        //
                    }
                }
            }
        }
        return ret;
    }

    private void insertStaticData(SQLiteDatabase db, String tableName, List<RecordContainer> records) {
        // TODO: records are inserted individually, but there is not a strong reason why they should--investigate batching instead of individual record insertion
        for (RecordContainer record : records) {
            final List<String> columns = new ArrayList<>(record.keySet());
            String sql = Sql.generator().newSingleRowInsertionSql(tableName, columns);
            SQLiteStatement statement = db.compileStatement(sql);
            bindObjects(statement, columns, record);
            statement.executeInsert();  // TODO: figure out what to do with the return
            statement.close();
        }
    }
}
