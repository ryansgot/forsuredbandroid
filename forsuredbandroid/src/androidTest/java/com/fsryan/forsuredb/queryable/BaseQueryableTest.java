package com.fsryan.forsuredb.queryable;

import android.database.sqlite.SQLiteDatabase;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.FSTableCreator;
import com.fsryan.forsuredb.gsonserialization.FSDbInfoGsonSerializer;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.LinkedList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getTargetContext;

public abstract class BaseQueryableTest {

    public static final String AUTHORITY = "com.fsryan.forsuredb.test.content";

    private static final String dbName = "test.db";

    @BeforeClass
    public static void setUpForSureAndroidInfoFactory() {
        getTargetContext().deleteDatabase("test.db");

        final List<FSTableCreator> tableList = new LinkedList<>();
        // the api class doesn't realy matter for the purpose of the test environment
        tableList.add(new FSTableCreator(AUTHORITY, "additional_data", FSGetApi.class));
        tableList.add(new FSTableCreator(AUTHORITY, "doc_store_test", FSGetApi.class));
        tableList.add(new FSTableCreator(AUTHORITY, "profile_info", FSGetApi.class));
        tableList.add(new FSTableCreator(AUTHORITY, "user", FSGetApi.class));

        // Will apply migrations in the assets directory
        FSDBHelper.initDebug(getTargetContext(), dbName, tableList, new FSDbInfoGsonSerializer());
    }

    @AfterClass
    public static void deleteDatabase() {
        getTargetContext().deleteDatabase("test.db");
    }

    @Before
    public void deleteDataFromTables() throws Exception {
        SQLiteDatabase db = FSDBHelper.inst().getWritableDatabase();
        db.delete("additional_data", null, null);
        db.delete("doc_store_test", null, null);
        db.delete("profile_info", null, null);
        db.delete("user", null, null);
    }
}
