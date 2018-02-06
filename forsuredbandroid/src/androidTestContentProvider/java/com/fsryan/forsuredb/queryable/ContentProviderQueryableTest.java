package com.fsryan.forsuredb.queryable;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.util.Pair;

import com.fsryan.forsuredb.ForSureAndroidInfoFactory;
import com.fsryan.forsuredb.api.FSQueryable;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static android.support.test.InstrumentationRegistry.getTargetContext;

@RunWith(AndroidJUnit4.class)
public class ContentProviderQueryableTest extends BasicQueryableTestsWithSeedDataInAssets<Uri> {

    @BeforeClass
    public static void initForsureAndroidInfoFactory() {
        ForSureAndroidInfoFactory.init(getTargetContext(), AUTHORITY);
    }

    @AfterClass
    public static void tearDownForSureAndroidInfoFactory() throws Exception {
        Field factory = ForSureAndroidInfoFactory.class.getDeclaredField("instance");
        factory.setAccessible(true);
        factory.set(null, null);
    }

    @Override
    protected Uri recordLocator(String table, long id) {
        return ForSureAndroidInfoFactory.inst().locatorFor(table, id);
    }

    @Override
    protected Uri tableLocator(String table, Pair<String, String>... joinKVPairs) {
        Uri uri = ForSureAndroidInfoFactory.inst().tableResource(table);
        if (joinKVPairs.length == 0) {
            return uri;
        }

        Uri.Builder builder = uri.buildUpon();
        for (Pair<String, String> joinKVPair : joinKVPairs) {
            builder.appendQueryParameter(joinKVPair.first, joinKVPair.second);
        }
        return builder.build();
    }

    @Override
    protected long idFrom(Uri insertedRecord) {
        return Long.parseLong(insertedRecord.getLastPathSegment());
    }

    @Override
    protected FSQueryable<Uri, FSContentValues> createQueryable(Uri locator) {
        return new ContentProviderQueryable(getTargetContext(), locator);
    }
}
