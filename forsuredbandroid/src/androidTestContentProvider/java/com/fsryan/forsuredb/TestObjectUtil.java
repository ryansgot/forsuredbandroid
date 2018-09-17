package com.fsryan.forsuredb;

import android.net.Uri;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.Limits;
import com.fsryan.forsuredb.api.OrderBy;
import com.fsryan.forsuredb.queryable.BaseQueryableTest;
import com.fsryan.forsuredb.queryable.UriAnalyzer;

import java.util.HashMap;
import java.util.Map;

public abstract class TestObjectUtil {

    public static Uri starterUri() {
        return starterUri("table");
    }

    public static Uri starterUri(String tableName) {
        return new Uri.Builder()
                .authority(BaseQueryableTest.AUTHORITY)
                .scheme("content")
                .appendPath(tableName)
                .build();
    }

    public static Uri starterUri(String tableName, long recordId) {
        return starterUri(tableName).buildUpon().appendPath(Long.toString(recordId)).build();
    }

    public static Uri starterUri(long recordId) {
        return starterUri().buildUpon().appendPath(Long.toString(recordId)).build();
    }

    public static Uri tableUriWithJoins(FSJoin... joins) {
        return tableUriWithJoins("table", joins);
    }

    public static Uri tableUriWithJoins(String tableName, FSJoin... joins) {
        if (joins == null || joins.length == 0) {
            return starterUri(tableName);
        }

        Uri.Builder builder = addJoinParameter(starterUri(tableName), joins[0]);
        for (int i = 1; i < joins.length; i++) {
            addJoinParameter(builder, joins[i]);
        }
        return builder.build();
    }

    public static Uri tableUriWithOrderings(FSOrdering... orderings) {
        if (orderings == null || orderings.length == 0) {
            return starterUri();
        }

        Uri.Builder builder = addOrderingTo(starterUri(), orderings[0]);
        for (int i = 1; i < orderings.length; i++) {
            addOrderingTo(builder, orderings[i]);
        }
        return builder.build();
    }

    public static Uri.Builder makeDistinct(Uri uri) {
        return makeDistinct(uri.buildUpon());
    }

    public static Uri.Builder makeDistinct(Uri.Builder builder) {
        return builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_DISTINCT, String.valueOf(true));
    }

    public static Uri.Builder makeUpsert(Uri uri) {
        return makeUpsert(uri.buildUpon());
    }

    public static Uri.Builder makeUpsert(Uri.Builder builder) {
        return builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_UPSERT, String.valueOf(true));
    }

    public static FSJoin createFSJoin(FSJoin.Type type, int equivaliences) {
        Map<String, String> childToParentColumnMap = new HashMap<>(equivaliences * 2);
        for (int i = 1; i <= equivaliences; i++) {
            childToParentColumnMap.put("child_column" + i, "parent_column" + i);
        }
        return createFSJoin(type, childToParentColumnMap);
    }

    public static FSJoin createSize1FSJoin(FSJoin.Type type, String child, String parent) {
        Map<String, String> childToParentColumnMap = new HashMap<>(1);
        childToParentColumnMap.put(child + "_column1", parent + "column1");
        return new FSJoin(type, parent, child, childToParentColumnMap);
    }

    public static FSJoin createFSJoin(FSJoin.Type type, Map<String, String> childToParentColumnMap) {
        return new FSJoin(type, "parent", "child", childToParentColumnMap);
    }

    public static FSOrdering orderingAsc() {
        return orderingAsc("table", "column");
    }

    public static FSOrdering orderingAsc(String table, String column) {
        return new FSOrdering(table, column, OrderBy.ORDER_ASC);
    }

    public static FSOrdering orderingDesc() {
        return orderingDesc("table", "column");
    }

    public static FSOrdering orderingDesc(String table, String column) {
        return new FSOrdering(table, column, OrderBy.ORDER_DESC);
    }

    public static Uri.Builder addOrderingTo(Uri uri, FSOrdering ordering) {
        return addOrderingTo(uri.buildUpon(), ordering);
    }

    public static Uri.Builder addOrderingTo(Uri.Builder builder, FSOrdering ordering) {
        return builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_ORDERING, UriAnalyzer.stringify(ordering));
    }

    public static Uri.Builder addJoinParameter(Uri.Builder builder, FSJoin fsJoin) {
        return builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_JOIN, UriAnalyzer.stringify(fsJoin));
    }

    public static Uri.Builder addJoinParameter(Uri uri, FSJoin fsJoin) {
        return addJoinParameter(uri.buildUpon(), fsJoin);
    }

    public static Uri.Builder addLimitsTo(Uri.Builder builder, Limits limits) {
        return builder.appendQueryParameter(UriAnalyzer.QUERY_PARAM_LIMITS, UriAnalyzer.stringify(limits));
    }

    public static Uri.Builder addLimitsTo(Uri uri, Limits limits) {
        return addLimitsTo(uri.buildUpon(), limits);
    }

    public static Uri tableUriWithLimits(Limits limits) {
        return addLimitsTo(starterUri(), limits).build();
    }

    public static Limits createLimits(final int count, final int offset, final boolean fromBottom) {
        return new Limits() {
            @Override
            public int count() {
                return count;
            }

            @Override
            public int offset() {
                return offset;
            }

            @Override
            public boolean isBottom() {
                return fromBottom;
            }
        };
    }
}
