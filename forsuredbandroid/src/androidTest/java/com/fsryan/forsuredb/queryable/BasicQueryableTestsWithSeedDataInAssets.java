package com.fsryan.forsuredb.queryable;

import android.content.ContentValues;
import android.support.v4.util.Pair;
import android.util.Log;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.fsryan.forsuredb.TestQueryUtil.idOrderingASC;
import static com.fsryan.forsuredb.TestQueryUtil.idSelection;
import static com.fsryan.forsuredb.TestQueryUtil.orderings;
import static com.fsryan.forsuredb.TestQueryUtil.selection;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class BasicQueryableTestsWithSeedDataInAssets<L> extends BaseQueryableTest {

    static final double ACCEPTABLE_DELTA = 0.000001D;

    static final String[] userTableColumns = new String[] {
            "_id",
            "app_rating",
            "competitor_app_rating",
            "created","deleted",
            "global_id",
            "login_count",
            "modified"
    };
    static final String[] profileInfoTableColumns = new String[] {
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

    @Rule
    public TestName testName = new TestName();

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

        assertUserValueInCursorAtPosition(r, 0, 1L, standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
    }

    @Test
    public void shouldUpdateSpecificRecordAndRetrieveUpdatedValue() {
        insertStandardUser();

        final FSContentValues update = new FSContentValues(userCV(3.1, BigDecimal.ONE, 2L, 3));
        int rowsAffected = createQueryable(userRecordLocator(1L)).update(update, null, null);
        assertEquals(1, rowsAffected);

        r = createQueryable(userRecordLocator(1L)).query(userTableProjection(), null, null);
        assertUserValueInCursorAtPosition(r, 0, 1L, 3.1, BigDecimal.ONE, 2L, 3);
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
        assertUserValueInCursorAtPosition(r, 0, 1L, standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
        assertProfileInfoValueInCursorAtPosition(r, 0, standardProfileInfoUserId, standardProfileInfoEmailAddress, standardProfileInfoUuid, standardProfileInfoAwesome, standardProfileInfoBinaryData);
    }

    @Test
    public void shouldInsertWhenUpsertCalledAndNoMatches() {
        L locator = insertStandardUser();

        final FSContentValues update = new FSContentValues(userCV(3.1, BigDecimal.ONE, 2L, 3));
        // id 2 does not exist in database yet
        SaveResult<L> result = createQueryable(userTableLocator()).upsert(update, idSelection(2L), null);
        assertEquals(1, result.rowsAffected());

        r = createQueryable(userTableLocator()).query(userTableProjection(),null, null);
        assertUserValueInCursorAtPosition(r, 0, 1L, standardUserAppRating, standardUserCompetitorAppRating, standardUserGlobalId, standardUserLoginCount);
        assertUserValueInCursorAtPosition(r, 1, 2L, 3.1, BigDecimal.ONE, 2L, 3);
    }

    @Test
    public void shouldUpdateWhenUpsertCalledAndHasMatches() {
        insertStandardUser();

        final FSContentValues update = new FSContentValues(userCV(3.1, BigDecimal.ONE, 2L, 3));
        // id 1 was just inserted above
        SaveResult<L> result = createQueryable(userTableLocator()).upsert(update, idSelection(1L), null);
        assertEquals(1, result.rowsAffected());

        r = createQueryable(userTableLocator()).query(userTableProjection(), idSelection(1L), null);
        assertUserValueInCursorAtPosition(r, 0, 1L, 3.1, BigDecimal.ONE, 2L, 3);
    }

    @Test
    public void shouldCorectlyLimitOnRetrievalQueryFromTop() {
        insertConsecutivelyIncreasingValuedUsers(4);

        FSSelection selection = selection().limitCount(2).build();
        r = createQueryable(userTableLocator()).query(userTableProjection(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                1L,
                standardUserAppRating,
                standardUserCompetitorAppRating,
                standardUserGlobalId,
                standardUserLoginCount
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertEquals(2, r.getCount());
    }

    @Test
    public void shouldCorectlyLimitOnRetrievalQueryFromBottom() {
        insertConsecutivelyIncreasingValuedUsers(4);

        FSSelection selection = selection().limitCount(2).limitFromBottom(true).build();
        r = createQueryable(userTableLocator()).query(userTableProjection(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                4L,
                standardUserAppRating + 3,
                standardUserCompetitorAppRating.add(new BigDecimal(3)),
                standardUserGlobalId + 3,
                standardUserLoginCount + 3
        );
        assertEquals(2, r.getCount());
    }

    @Test
    public void shouldCorrectlyOffsetQueryWithoutLimitFromTop() {
        insertConsecutivelyIncreasingValuedUsers(4);

        FSSelection selection = selection().offset(1).build();
        r = createQueryable(userTableLocator()).query(userTableProjection(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertUserValueInCursorAtPosition(
                r,
                2,
                4L,
                standardUserAppRating + 3,
                standardUserCompetitorAppRating.add(new BigDecimal(3)),
                standardUserGlobalId + 3,
                standardUserLoginCount + 3
        );
        assertEquals(3, r.getCount());
    }

    @Test
    public void shouldCorrectlyOffsetQueryWithoutLimitFromBottom() {
        insertConsecutivelyIncreasingValuedUsers(4);

        FSSelection selection = selection().offset(1).limitFromBottom(true).build();
        r = createQueryable(userTableLocator()).query(userTableProjection(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                1L,
                standardUserAppRating,
                standardUserCompetitorAppRating,
                standardUserGlobalId,
                standardUserLoginCount
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertUserValueInCursorAtPosition(
                r,
                2,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertEquals(3, r.getCount());
    }

    @Test
    public void shouldCorrectlyOffsetQueryWithLimitFromTop() {
        insertConsecutivelyIncreasingValuedUsers(4);

        FSSelection selection = selection().offset(1).limitCount(2).build();
        r = createQueryable(userTableLocator()).query(userTableProjection(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertEquals(2, r.getCount());
    }

    @Test
    public void shouldCorrectlyOffsetQueryWithLimitFromBottom() {
        insertConsecutivelyIncreasingValuedUsers(4);

        FSSelection selection = selection().offset(1).limitCount(2).limitFromBottom(true).build();
        r = createQueryable(userTableLocator()).query(userTableProjection(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertEquals(2, r.getCount());
    }

    @Test
    public void shouldCorectlyLimitOnRetrievalJoinQueryFromTop() {
        insertConsecutivelyIncreasingValuedUsers(4, true);

        FSSelection selection = selection().limitCount(2).build();
        r = createQueryable(userProfileInfoJoinLocator())
                .query(userProfileInfoJoin(), userProfileInfoJoinProjections(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                1L,
                standardUserAppRating,
                standardUserCompetitorAppRating,
                standardUserGlobalId,
                standardUserLoginCount
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertEquals(2, r.getCount());
    }

    @Test
    public void shouldCorectlyLimitOnRetrievalJoinQueryFromBottom() {
        insertConsecutivelyIncreasingValuedUsers(4, true);

        FSSelection selection = selection().limitCount(2).limitFromBottom(true).build();
        r = createQueryable(userProfileInfoJoinLocator())
                .query(userProfileInfoJoin(), userProfileInfoJoinProjections(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                4L,
                standardUserAppRating + 3,
                standardUserCompetitorAppRating.add(new BigDecimal(3)),
                standardUserGlobalId + 3,
                standardUserLoginCount + 3
        );
        assertEquals(2, r.getCount());
    }

    @Test
    public void shouldCorrectlyOffsetJoinQueryWithoutLimitFromTop() {
        insertConsecutivelyIncreasingValuedUsers(4, true);

        FSSelection selection = selection().offset(1).build();
        r = createQueryable(userProfileInfoJoinLocator())
                .query(userProfileInfoJoin(), userProfileInfoJoinProjections(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertUserValueInCursorAtPosition(
                r,
                2,
                4L,
                standardUserAppRating + 3,
                standardUserCompetitorAppRating.add(new BigDecimal(3)),
                standardUserGlobalId + 3,
                standardUserLoginCount + 3
        );
        assertEquals(3, r.getCount());
    }

    @Test
    public void shouldCorrectlyOffsetJoinQueryWithoutLimitFromBottom() {
        insertConsecutivelyIncreasingValuedUsers(4, true);

        FSSelection selection = selection().offset(1).limitFromBottom(true).build();
        r = createQueryable(userProfileInfoJoinLocator())
                .query(userProfileInfoJoin(), userProfileInfoJoinProjections(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                1L,
                standardUserAppRating,
                standardUserCompetitorAppRating,
                standardUserGlobalId,
                standardUserLoginCount
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertUserValueInCursorAtPosition(
                r,
                2,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertEquals(3, r.getCount());
    }

    @Test
    public void shouldCorrectlyOffsetJoinQueryWithLimitFromTop() {
        insertConsecutivelyIncreasingValuedUsers(4, true);

        FSSelection selection = selection().offset(1).limitCount(2).build();
        r = createQueryable(userProfileInfoJoinLocator())
                .query(userProfileInfoJoin(), userProfileInfoJoinProjections(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertEquals(2, r.getCount());
    }

    @Test
    public void shouldCorrectlyOffsetJoinQueryWithLimitFromBottom() {
        insertConsecutivelyIncreasingValuedUsers(4, true);

        FSSelection selection = selection().offset(1).limitCount(2).limitFromBottom(true).build();
        r = createQueryable(userProfileInfoJoinLocator())
                .query(userProfileInfoJoin(), userProfileInfoJoinProjections(), selection, orderings(idOrderingASC("user")));

        assertUserValueInCursorAtPosition(
                r,
                0,
                2L,
                standardUserAppRating + 1,
                standardUserCompetitorAppRating.add(BigDecimal.ONE),
                standardUserGlobalId + 1,
                standardUserLoginCount + 1
        );
        assertUserValueInCursorAtPosition(
                r,
                1,
                3L,
                standardUserAppRating + 2,
                standardUserCompetitorAppRating.add(new BigDecimal(2)),
                standardUserGlobalId + 2,
                standardUserLoginCount + 2
        );
        assertEquals(2, r.getCount());
    }

    protected abstract long idFrom(L insertedRecord);

    protected abstract FSQueryable<L, FSContentValues> createQueryable(L locator);

    protected abstract L recordLocator(String table, long id);

    protected abstract L tableLocator(String table, Pair<String, String>... joinStringKVPair);

    private static List<FSProjection> userProfileInfoJoinProjections() {
        return Arrays.asList(profileInfoTableProjection(), userTableProjection());
    }

    private static List<FSJoin> userProfileInfoJoin() {
        return Arrays.asList(new FSJoin(FSJoin.Type.INNER, "user", "profile_info", ImmutableMap.of("user_id", "_id")));
    }

    private static ContentValues userCV(double appRating, BigDecimal competitorAppRating, long globalId, int loginCount) {
        ContentValues ret = new ContentValues();
        ret.put("app_rating", appRating);
        ret.put("competitor_app_rating", competitorAppRating.toString());
        ret.put("global_id", globalId);
        ret.put("login_count", loginCount);
        return ret;
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

    private void assertUserValueInCursorAtPosition(Retriever r, int position, long expectedId, double expectedAppRating, BigDecimal expectedCompetitorAppRating, long expectedGlobalId, int expectedLoginCount) {
        assertTrue(r.moveToPosition(position));
        long actualId = r.getLong("user__id");
        double actualAppRating = r.getDouble("user_app_rating");
        BigDecimal actualCompetitorAppRating = new BigDecimal(r.getString("user_competitor_app_rating"));
        long actualGlobalId = r.getLong("user_global_id");
        int actualLoginCount = r.getInt("user_login_count");

        Log.i("QUERY_CHECK", testName.getMethodName() + ": position = " + position
                + "\n\t_id = " + actualId + ", expected = " + expectedId
                + "\n\tuser_app_rating = " + actualAppRating + ", expected " + expectedAppRating
                + "\n\tuser_competitor_app_rating = " + actualCompetitorAppRating + ", expected = " + expectedCompetitorAppRating
                + "\n\tuser_global_id = " + actualGlobalId + ", expected = " + expectedGlobalId
                + "\n\tuser_login_count = " + actualLoginCount + ", expected = " + expectedLoginCount);

        assertEquals(expectedId, actualId);
        assertEquals(expectedAppRating, actualAppRating, ACCEPTABLE_DELTA);
        assertEquals(expectedCompetitorAppRating, actualCompetitorAppRating);
        assertEquals(expectedGlobalId, actualGlobalId);
        assertEquals(expectedLoginCount, actualLoginCount);
    }

    private L userProfileInfoJoinLocator() {
        return profileInfoTableLocator(new Pair<>("INNER JOIN", "user ON user._id = profile_info.user_id"));
    }

    private void insertConsecutivelyIncreasingValuedUsers(int count) {
        insertConsecutivelyIncreasingValuedUsers(count, false);
    }

    private void insertConsecutivelyIncreasingValuedUsers(int count, boolean createProfileInfo) {
        double appRating = standardUserAppRating;
        BigDecimal competitorAppRating = standardUserCompetitorAppRating;
        long globalId = standardUserGlobalId;
        int loginCount = standardUserLoginCount;
        for (int i = 0; i < count; i++) {
            L inserted = insertUser(appRating, competitorAppRating, globalId, loginCount);
            if (createProfileInfo) {
                long userId = idFrom(inserted);
                String email = "user" + userId + "@email.com";
                insertProfileInfo(userId, email, email, userId % 2 == 0, new byte[] {(byte) (userId & 0x000000FF)});
            }
            appRating += 1;
            competitorAppRating = competitorAppRating.add(BigDecimal.ONE);
            globalId += 1;
            loginCount += 1;
        }
    }

    private void assertProfileInfoValueInCursorAtPosition(Retriever c, int position, long userId, String emailAddress, String uuid, boolean awesome, byte[] binaryData) {
        assertTrue(c.moveToPosition(position));
        assertEquals(userId, c.getLong("profile_info_user_id"));
        assertEquals(emailAddress, c.getString("profile_info_email_address"));
        assertEquals(uuid, c.getString("profile_info_uuid"));
        assertEquals(awesome ? 1 : 0, c.getInt("profile_info_awesome"));
        assertArrayEquals(binaryData, c.getBytes("profile_info_binary_data"));
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
