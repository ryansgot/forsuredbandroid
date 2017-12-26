package com.fsryan.forsuredb.queryable;

import android.content.ContentValues;
import android.support.v4.util.Pair;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Limits;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class BasicQueryableTestsWithSeedDataInAssets<L> extends BaseQueryableTest {

    /*package*/ static final double ACCEPTABLE_DELTA = 0.000001D;

    /*package*/ static final String[] userTableColumns = new String[] {
            "_id",
            "app_rating",
            "competitor_app_rating",
            "created","deleted",
            "global_id",
            "login_count",
            "modified"
    };
    /*package*/ static final String[] profileInfoTableColumns = new String[] {
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

    private Retriever r;

    @Before
    public void resetRetriever() throws Exception {
        r = null;
    }

    @After
    public void closeRetriever() {
        if (r != null) {
            r.close();
        }
    }

    @Test
    public void shouldInsertAndRetrieveInsertedValue() {
        assertEquals(userRecordLocator(1L), insertStandardUser());

        r = createQueryable(userRecordLocator(1L)).query(userTableProjection(), null, null);

        assertUserValueInCursorAtPosition(r, 0, standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
    }

    @Test
    public void shouldUpdateSpecificRecordAndRetrieveUpdatedValue() {
        insertStandardUser();

        final FSContentValues update = new FSContentValues(userCV(3.1, BigDecimal.ONE, 2L, 3));
        int rowsAffected = createQueryable(userRecordLocator(1L)).update(update, null, null);
        assertEquals(1, rowsAffected);

        r = createQueryable(userRecordLocator(1L)).query(userTableProjection(), null, null);
        assertUserValueInCursorAtPosition(r, 0, 3.1, BigDecimal.ONE, 2L, 3);
    }

    @Test
    public void shouldDeleteSpecificRecord() {
        insertStandardUser();

        int rowsAffected = createQueryable(userRecordLocator(1L)).delete(null, null);
        assertEquals(1, rowsAffected);
    }

    @Test
    public void shouldProperlyJoin() {
        insertStandardUser();
        assertEquals(profileInfoRecordLocator(1L), insertStandardProfileInfo());

        L toQuery = profileInfoTableLocator(new Pair<>("INNER JOIN", "user ON user._id = profile_info.user_id"));
        List<FSJoin> joins = Arrays.asList(new FSJoin(FSJoin.Type.INNER, "user", "profile_info", ImmutableMap.of("user_id", "_id")));
        List<FSProjection> projections = Arrays.asList(profileInfoTableProjection(), userTableProjection());

        r = createQueryable(toQuery).query(joins, projections, null, null);
        assertUserValueInCursorAtPosition(r, 0, standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
        assertProfileInfoValueInCursorAtPosition(r, 0, standardProfileInfoUserId, standardProfileInfoEmailAddress, standardProfileInfoUuid, standardProfileInfoAwesome, standardProfileInfoBinaryData);
    }

    @Test
    public void shouldInsertWhenUpsertCalledAndNoMatches() {
        L locator = insertStandardUser();

        final FSContentValues update = new FSContentValues(userCV(3.1, BigDecimal.ONE, 2L, 3));
        // id 2 does not exist in database yet
        SaveResult<L> result = createQueryable(userTableLocator()).upsert(update, createIdSelection(2L), null);
        assertEquals(1, result.rowsAffected());

        r = createQueryable(userTableLocator()).query(userTableProjection(),null, null);
        assertUserValueInCursorAtPosition(r, 0, standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
        assertUserValueInCursorAtPosition(r, 1, 3.1, BigDecimal.ONE, 2L, 3);
    }

    @Test
    public void shouldUpdateWhenUpsertCalledAndHasMatches() {
        insertStandardUser();

        final FSContentValues update = new FSContentValues(userCV(3.1, BigDecimal.ONE, 2L, 3));
        // id 1 was just inserted above
        SaveResult<L> result = createQueryable(userTableLocator()).upsert(update, createIdSelection(1L), null);
        assertEquals(1, result.rowsAffected());

        r = createQueryable(userTableLocator()).query(userTableProjection(), createIdSelection(1L), null);
        assertUserValueInCursorAtPosition(r, 0, 3.1, BigDecimal.ONE, 2L, 3);
    }

    protected abstract FSQueryable<L, FSContentValues> createQueryable(L locator);

    protected abstract L recordLocator(String table, long id);

    protected abstract L tableLocator(String table, Pair<String, String>... joinStringKVPair);

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
        assertArrayEquals(binaryData, c.getBytes("profile_info_binary_data"));
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

    private static FSSelection createIdSelection(final long id) {
        return new FSSelection() {
            @Override
            public String where() {
                return "_id=?";
            }

            @Override
            public String[] replacements() {
                return new String[] {Long.toString(id)};
            }

            @Override
            public Limits limits() {
                return null;
            }
        };
    }

    private L insertStandardUser() {
        return insertUser(standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
    }

    private L insertUser(double appRating, BigDecimal competitorAppRating, long globalId, int loginCount) {
        ContentValues inputCV = userCV(appRating, competitorAppRating, globalId, loginCount);
        return createQueryable(userTableLocator()).insert(new FSContentValues(inputCV));
    }

    private L insertStandardProfileInfo() {
        return insertProfileInfo(standardProfileInfoUserId, standardProfileInfoEmailAddress, standardProfileInfoUuid, standardProfileInfoAwesome, standardProfileInfoBinaryData);
    }

    private L insertProfileInfo(long userId, String emailAddress, String uuid, boolean awesome, byte[] binaryData) {
        ContentValues inputCV = profileInfoCV(userId, emailAddress, uuid, awesome, binaryData);
        return createQueryable(profileInfoTableLocator()).insert(new FSContentValues(inputCV));
    }

    private L userRecordLocator(long id) {
        return recordLocator("user", id);
    }

    private L profileInfoRecordLocator(long id) {
        return recordLocator("profile_info", id);
    }

    private L userTableLocator(Pair<String, String>... joinKVPairs) {
        return tableLocator("user", joinKVPairs);
    }

    private L profileInfoTableLocator(Pair<String, String>... joinKVPairs) {
        return tableLocator("profile_info", joinKVPairs);
    }
}
