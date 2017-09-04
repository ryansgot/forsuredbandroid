package com.fsryan.forsuredb.queryable;

import android.content.ContentValues;
import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import com.fsryan.forsuredb.ForSureAndroidInfoFactory;
import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.Retriever;
import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ContentProviderQueryableTest extends BaseQueryableTest {

    private static final double ACCEPTABLE_DELTA = 0.000001D;

    private static final String[] userTableColumns = new String[] {
            "_id",
            "app_rating",
            "competitor_app_rating",
            "created","deleted",
            "global_id",
            "login_count",
            "modified"
    };
    private static final String[] profileInfoTableColumns = new String[] {
            "_id",
            "awesome",
            "binary_data",
            "created",
            "deleted",
            "email_address",
            "modified",
            "user_id",
            "uuid"
    };

    private static final double standardUserAppRating = 4.1D;
    private static final BigDecimal standardUserCompetitorAppRating = new BigDecimal("4.2");
    private static final long standardUserGlobalId = 1L;
    private static final int standardUserLoginCount = 45;

    private static final long standardProfileInfoUserId = 1L;
    private static final String standardProfileInfoEmailAddress = "standard@profileinfo.museum";
    private static final String standardProfileInfoUuid = UUID.randomUUID().toString();
    private static final boolean standardProfileInfoAwesome = true;
    private static final byte[] standardProfileInfoBinaryData = new byte[] {19, 85, 3, 11};

    private static Uri userTableUri;
    private static Uri profileInfoTableUri;

    private Retriever r;

    @BeforeClass
    public static void initUris() {
        userTableUri = ForSureAndroidInfoFactory.inst().tableResource("user");
        profileInfoTableUri = ForSureAndroidInfoFactory.inst().tableResource("profile_info");
    }

    @Before
    public void nullifyCursor() {
        r = null;
    }

    @After
    public void closeCursor() {
        if (r != null) {
            r.close();
        }
    }

    @Test
    public void shouldInsertAndRetrieveInsertedValue() {
        assertEquals(recordUri("user", 1L), insertStandardUser());

        r = cpQueryable(recordUri("user", 1L)).query(userTableProjection(), null, null);

        assertUserValueInCursorAtPosition(r, 0, standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
    }

    @Test
    public void shouldUpdateSpecificRecordAndRetrieveUpdatedValue() {
        insertStandardUser();

        final FSContentValues update = new FSContentValues(userCV(3.1, BigDecimal.ONE, 2L, 3));
        int rowsAffected = cpQueryable(recordUri("user", 1L)).update(update, null, null);
        assertEquals(1, rowsAffected);

        r = cpQueryable(recordUri("user", 1L)).query(userTableProjection(), null, null);
        assertUserValueInCursorAtPosition(r, 0, 3.1, BigDecimal.ONE, 2L, 3);
    }

    @Test
    public void shouldDeleteSpecificRecord() {
        insertStandardUser();

        int rowsAffected = cpQueryable(recordUri("user", 1L)).delete(null, null);
        assertEquals(1, rowsAffected);
    }

    @Test
    public void shouldProperlyJoin() {
        insertStandardUser();
        assertEquals(recordUri("profile_info", 1L), insertStandardProfileInfo());

        Uri toQuery = profileInfoTableUri.buildUpon()
                .appendQueryParameter("INNER JOIN", "user ON user._id = profile_info.user_id")
                .build();
        List<FSJoin> joins = Arrays.asList(new FSJoin(FSJoin.Type.INNER, "user", "profile_info", ImmutableMap.of("user_id", "_id")));
        List<FSProjection> projections = Arrays.asList(profileInfoTableProjection(), userTableProjection());

        r = cpQueryable(toQuery).query(joins, projections, null, null);
        assertUserValueInCursorAtPosition(r, 0, standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
        assertProfileInfoValueInCursorAtPosition(r, 0, standardProfileInfoUserId, standardProfileInfoEmailAddress, standardProfileInfoUuid, standardProfileInfoAwesome, standardProfileInfoBinaryData);
    }

    private Uri insertStandardUser() {
        return insertUser(standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
    }

    private Uri insertUser(double appRating, BigDecimal competitorAppRating, long globalId, int loginCount) {
        ContentValues inputCV = userCV(appRating, competitorAppRating, globalId, loginCount);
        return cpQueryable(userTableUri).insert(new FSContentValues(inputCV));
    }

    private Uri insertStandardProfileInfo() {
        return insertProfileInfo(standardProfileInfoUserId, standardProfileInfoEmailAddress, standardProfileInfoUuid, standardProfileInfoAwesome, standardProfileInfoBinaryData);
    }

    private Uri insertProfileInfo(long userId, String emailAddress, String uuid, boolean awesome, byte[] binaryData) {
        ContentValues inputCV = profileInfoCV(userId, emailAddress, uuid, awesome, binaryData);
        return cpQueryable(profileInfoTableUri).insert(new FSContentValues(inputCV));
    }

    private static FSProjection userTableProjection(String... columns) {
        return userTableProjection(false, columns);
    }

    private static FSProjection userTableProjection(final boolean distinct, final String... columns) {
        return new FSProjection() {
            @Override
            public String tableName() {
                return "user";
            }

            @Override
            public String[] columns() {
                return columns.length == 0 ? userTableColumns : columns;
            }

            @Override
            public boolean isDistinct() {
                return distinct;
            }
        };
    }

    private static FSProjection profileInfoTableProjection(String... columns) {
        return profileInfoTableProjection(false, columns);
    }

    private static FSProjection profileInfoTableProjection(final boolean distinct, final String... columns) {
        return new FSProjection() {
            @Override
            public String tableName() {
                return "profile_info";
            }

            @Override
            public String[] columns() {
                return columns.length == 0 ? profileInfoTableColumns : columns;
            }

            @Override
            public boolean isDistinct() {
                return distinct;
            }
        };
    }

    private static Uri recordUri(String table, long id) {
        return ForSureAndroidInfoFactory.inst().locatorFor(table, 1L);
    }

    private static ContentProviderQueryable cpQueryable(Uri locator) {
        return new ContentProviderQueryable(getTargetContext(), locator);
    }

    private static void assertUserValueInCursorAtPosition(Retriever r, int position, double appRating, BigDecimal competitorAppRating, long globalId, int loginCount) {
        assertTrue(r.moveToPosition(position));
        assertEquals(appRating, r.getDouble("user_app_rating"), ACCEPTABLE_DELTA);
        assertEquals(competitorAppRating.toString(), r.getString("user_competitor_app_rating"));
        assertEquals(globalId, r.getLong("user_global_id"));
        assertEquals(loginCount, r.getInt("user_login_count"));
    }

    private static ContentValues userCV(double appRating, BigDecimal competitorAppRating, long globalId, int loginCount) {
        ContentValues ret = new ContentValues();
        ret.put("app_rating", appRating);
        ret.put("competitor_app_rating", competitorAppRating.toString());
        ret.put("global_id", globalId);
        ret.put("login_count", loginCount);
        return ret;
    }

    private void assertProfileInfoValueInCursorAtPosition(Retriever c, int position, long userId, String emailAddress, String uuid, boolean awesome, byte[] binaryData) {
        assertTrue(c.moveToPosition(position));
        assertEquals(userId, c.getLong("profile_info_user_id"));
        assertEquals(emailAddress, c.getString("profile_info_email_address"));
        assertEquals(uuid, c.getString("profile_info_uuid"));
        assertEquals(awesome ? 1 : 0, c.getInt("profile_info_awesome"));
        assertArrayEquals(binaryData, c.getBlob("profile_info_binary_data"));
    }

    private static ContentValues profileInfoCV(long userId, String emailAddress, String uuid, boolean awesome, byte[] binaryData) {
        ContentValues ret = new ContentValues();
        ret.put("user_id", userId);
        ret.put("email_address", emailAddress);
        ret.put("uuid", uuid);
        ret.put("awesome", awesome);
        ret.put("binary_data", binaryData);
        return ret;
    }
}
