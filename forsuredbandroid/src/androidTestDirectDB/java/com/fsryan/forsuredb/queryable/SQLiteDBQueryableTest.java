package com.fsryan.forsuredb.queryable;

import android.support.test.runner.AndroidJUnit4;
import android.support.v4.util.Pair;

import com.fsryan.forsuredb.api.FSQueryable;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SQLiteDBQueryableTest extends BasicQueryableTestsWithSeedDataInAssets<DirectLocator> {

    @Test
    public void should() {}

    @Override
    protected FSQueryable<DirectLocator, FSContentValues> createQueryable(DirectLocator locator) {
        return new SQLiteDBQueryable(locator);
    }

    @Override
    protected DirectLocator recordLocator(String table, long id) {
        return new DirectLocator(table, id);
    }

    @Override
    protected DirectLocator tableLocator(String table, Pair<String, String>... joinStringKVPair) {
        // TODO: check that this works
        return new DirectLocator(table);
    }
}
