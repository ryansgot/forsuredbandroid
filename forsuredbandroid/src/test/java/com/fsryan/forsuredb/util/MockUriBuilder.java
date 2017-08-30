package com.fsryan.forsuredb.util;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.fsryan.forsuredb.api.info.JoinInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.fsryan.forsuredb.provider.UriEvaluator.DISTINCT_QUERY_PARAM;
import static com.fsryan.forsuredb.provider.UriEvaluator.FIRST_QUERY_PARAM;
import static com.fsryan.forsuredb.provider.UriEvaluator.LAST_QUERY_PARAM;
import static com.fsryan.forsuredb.provider.UriEvaluator.OFFSET_QUERY_PARAM;
import static com.fsryan.forsuredb.provider.UriEvaluator.ORDER_BY_QUERY_PARM;
import static com.fsryan.forsuredb.provider.UriEvaluator.isSpecificRecordUri;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockUriBuilder {

    private boolean distinct;
    private final List<JoinInfo> joins = new ArrayList<>();
    private final Set<String> queryParamNames = new HashSet<>();
    private String orderBy;
    private long specificRecord;
    private int offset;
    private int first;
    private int last;

    public MockUriBuilder distinct(boolean distinct) {
        this.distinct = distinct;
        queryParamNames.add(DISTINCT_QUERY_PARAM);
        return this;
    }

    public MockUriBuilder addJoin(JoinInfo joinInfo) {
        if (joinInfo != null) {
            joins.add(joinInfo);
            queryParamNames.add("JOIN");
        }
        return this;
    }

    public MockUriBuilder offset(int offset) {
        this.offset = offset;
        queryParamNames.add(OFFSET_QUERY_PARAM);
        return this;
    }

    public MockUriBuilder first(int first) {
        this.first = first;
        queryParamNames.add(FIRST_QUERY_PARAM);
        return this;
    }

    public MockUriBuilder last(int last) {
        this.last = last;
        queryParamNames.add(LAST_QUERY_PARAM);
        return this;
    }

    public MockUriBuilder orderBy(String orderBy) {
        this.orderBy = orderBy;
        if (orderBy != null) {
            queryParamNames.add(ORDER_BY_QUERY_PARM);
        } else {
            queryParamNames.remove(ORDER_BY_QUERY_PARM);
        }
        return this;
    }

    public MockUriBuilder specificRecord(long id) {
        this.specificRecord = id;
        return this;
    }

    public Uri build(@NonNull String tableName) {
        Uri uri = mock(Uri.class);
        when(uri.getQueryParameterNames()).thenReturn(queryParamNames);
        if (queryParamNames.contains(DISTINCT_QUERY_PARAM)) {
            when(uri.getQueryParameter(DISTINCT_QUERY_PARAM)).thenReturn(Boolean.toString(distinct));
        }
        if (queryParamNames.contains(OFFSET_QUERY_PARAM)) {
            when(uri.getQueryParameter(OFFSET_QUERY_PARAM)).thenReturn(Integer.toString(offset));
        }
        if (queryParamNames.contains(FIRST_QUERY_PARAM)) {
            when(uri.getQueryParameter(FIRST_QUERY_PARAM)).thenReturn(Integer.toString(first));
        }
        if (queryParamNames.contains(LAST_QUERY_PARAM)) {
            when(uri.getQueryParameter(LAST_QUERY_PARAM)).thenReturn(Integer.toString(last));
        }
        if (queryParamNames.contains(ORDER_BY_QUERY_PARM)) {
            when(uri.getQueryParameter(ORDER_BY_QUERY_PARM)).thenReturn(orderBy);
        }
        for (JoinInfo join : joins) {
            // TODO: implement this
        }

        if (specificRecord > 0L) {
            when(uri.getPathSegments()).thenReturn(Arrays.asList(tableName, Long.toString(specificRecord)));
            when(uri.getLastPathSegment()).thenReturn(Long.toString(specificRecord));
        } else {
            when(uri.getLastPathSegment()).thenReturn(tableName);
        }

        return uri;
    }
}
