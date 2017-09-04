package com.fsryan.forsuredb.queryable;

import android.database.sqlite.SQLiteDatabase;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureAndroidInfoFactory;
import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.FSTableCreator;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.LinkedList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getTargetContext;

public abstract class DefaultContentProviderTest {

    private static final String dbName = "test.db";
    private static final String authority = "com.fsryan.forsuredb.debug.content";

    @BeforeClass
    public static void setUpForSureAndroidInfoFactory() {

        getTargetContext().deleteDatabase("test.db");

        final List<FSTableCreator> tableList = new LinkedList<>();
        // the api class doesn't realy matter for the purpose of the test environment
        tableList.add(new FSTableCreator(authority, "additional_data", FSGetApi.class));
        tableList.add(new FSTableCreator(authority, "doc_store_test", FSGetApi.class));
        tableList.add(new FSTableCreator(authority, "profile_info", FSGetApi.class));
        tableList.add(new FSTableCreator(authority, "user", FSGetApi.class));

        // Will apply migrations in the assets directory
        FSDBHelper.initDebug(getTargetContext(), dbName, tableList);
        ForSureAndroidInfoFactory.init(getTargetContext(), authority);
    }

    @AfterClass
    public static void deleteDatabase() {
        getTargetContext().deleteDatabase("test.db");
    }

    @Before
    public void resetData() throws Exception {
        SQLiteDatabase db = FSDBHelper.inst().getWritableDatabase();
        db.delete("additional_data", null, null);
        db.delete("doc_store_test", null, null);
        db.delete("profile_info", null, null);
        db.delete("user", null, null);
    }
}
