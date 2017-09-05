package com.fsryan.forsuredb.util;

import android.net.Uri;

import com.fsryan.forsuredb.api.FSJoin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.fsryan.forsuredb.queryable.UriEvaluator.DISTINCT_QUERY_PARAM;
import static com.fsryan.forsuredb.queryable.UriEvaluator.FIRST_QUERY_PARAM;
import static com.fsryan.forsuredb.queryable.UriEvaluator.LAST_QUERY_PARAM;
import static com.fsryan.forsuredb.queryable.UriEvaluator.OFFSET_QUERY_PARAM;
import static com.fsryan.forsuredb.queryable.UriEvaluator.ORDER_BY_QUERY_PARM;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockUriBuilder {

    private StringBuilder queryBuf = new StringBuilder();
    private final Set<String> joinedTables = new HashSet<>();
    private final List<FSJoin> joins = new ArrayList<>();
    private final Map<String, List<String>> queryParamMap = new HashMap<>();
    private final List<String> queryParameterNames = new ArrayList<>();
    private long specificRecord;
    private final String tableName;

    public MockUriBuilder(String tableName) {
        this.tableName = tableName;
        joinedTables.add(tableName);
    }

    public MockUriBuilder distinct(boolean distinct) {
        addQueryParameter(DISTINCT_QUERY_PARAM, Boolean.toString(distinct));
        return this;
    }

    public MockUriBuilder addJoin(FSJoin.Type joinType, String parentTable, String childTable, String parentColumn, String childColumn, String... parentChildColumnArray) {
        Map<String, String> childToParentColumnMap = new HashMap<>(1 + parentChildColumnArray.length);
        childToParentColumnMap.put(childColumn, parentColumn);
        for (int i = 0; i < parentChildColumnArray.length; i += 2) {
            childToParentColumnMap.put(parentChildColumnArray[i + 1], parentChildColumnArray[i]);
        }
        return addJoin(new FSJoin(joinType, parentTable, childTable, childToParentColumnMap));
    }

    public MockUriBuilder addJoin(FSJoin join) {
        if (join != null) {
            joins.add(join);

            final String tableToJoin = joinedTables.contains(join.getParentTable()) ? join.getChildTable() : join.getParentTable();
            joinedTables.add(tableToJoin);
            StringBuilder value = new StringBuilder(tableToJoin).append(" ON ");
            for (Map.Entry<String, String> childToParentColumn : join.getChildToParentColumnMap().entrySet()) {
                value.append(join.getChildTable()).append('.').append(childToParentColumn.getKey())
                        .append(" = ").append(join.getParentTable()).append('.').append(childToParentColumn.getValue())
                        .append(" AND ");
            }
            addQueryParameter(join.getType().toString() + " JOIN", value.delete(value.length() - 5, value.length()).toString());
        }
        return this;
    }

    public MockUriBuilder offset(int offset) {
        addQueryParameter(OFFSET_QUERY_PARAM, Integer.toString(offset));
        return this;
    }

    public MockUriBuilder first(int first) {
        addQueryParameter(FIRST_QUERY_PARAM, Integer.toString(first));
        return this;
    }

    public MockUriBuilder last(int last) {
        addQueryParameter(LAST_QUERY_PARAM, Integer.toString(last));
        return this;
    }

    public MockUriBuilder orderBy(String orderBy) {
        addQueryParameter(ORDER_BY_QUERY_PARM, orderBy);
        return this;
    }

    public MockUriBuilder specificRecord(long id) {
        this.specificRecord = id;
        return this;
    }

    public Uri build() {
        Uri uri = mock(Uri.class);
        when(uri.getQueryParameterNames()).thenReturn(new HashSet<>(queryParameterNames));
        for (Map.Entry<String, List<String>> queryParamsWithSameKey : queryParamMap.entrySet()) {
            final String key = queryParamsWithSameKey.getKey();
            final List<String> values = queryParamsWithSameKey.getValue();
            for (int i = values.size() - 1; i >= 0; i--) {  // <-- return first added occurrence of the query parameter
                when(uri.getQueryParameter(eq(key))).thenReturn(values.get(i));
            }
            when(uri.getQueryParameters(eq(key))).thenReturn(values);
        }

        if (specificRecord > 0L) {
            when(uri.getPathSegments()).thenReturn(Arrays.asList(tableName, Long.toString(specificRecord)));
            when(uri.getLastPathSegment()).thenReturn(Long.toString(specificRecord));
        } else {
            when(uri.getLastPathSegment()).thenReturn(tableName);
        }

        when(uri.getQuery()).thenReturn(queryBuf.toString());

        return uri;
    }

    private void addQueryParameter(String key, String value) {
        queryParameterNames.add(key);
        if (queryBuf.length() != 0) {
            queryBuf.append('&');
        }
        queryBuf.append(key).append('=').append(value);
        putIntoMapList(key, value);
    }

    private void putIntoMapList(String key, String value) {
        List<String> existing = queryParamMap.get(key);
        if (existing == null) {
            existing = new ArrayList<>(1);
            queryParamMap.put(key, existing);
        }
        existing.add(value);
    }
}
