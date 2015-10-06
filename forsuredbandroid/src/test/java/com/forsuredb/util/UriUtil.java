package com.forsuredb.util;

import android.net.Uri;

import com.google.common.collect.Lists;

import org.mockito.Mockito;

import java.util.List;

public class UriUtil {

    private static final String authority = "com.forsuredb.util.content";

    private static final List<String> allRecordsJoinUri = Lists.newArrayList("parent", "*", "child");
    private static final List<String> allRecordsJoinWithSpecificParentMatch = Lists.newArrayList("parent", "1", "child");
    private static final List<String> specificRecordJoinWithSpecificParentMatch = Lists.newArrayList("parent", "1", "child", "1");

    public static Uri allRecordsUri() {
        return allRecordsUri("directory");
    }

    public static Uri allRecordsUri(String directory) {
        return mockUri(authority, directory);
    }

    public static Uri specificRecordUri(long id) {
        return specificRecordUri("directory", id);
    }

    public static Uri specificRecordUri(String directory, long id) {
        return mockUri(authority, directory, Long.toString(id));
    }

    public static Uri allRecordsJoinUri() {
        return mockUri(authority, allRecordsJoinUri.toArray(new String[allRecordsJoinUri.size()]));
    }

    public static Uri allRecordsJoinWithSpecificParentMatch() {
        return mockUri(authority, allRecordsJoinWithSpecificParentMatch.toArray(new String[allRecordsJoinWithSpecificParentMatch.size()]));
    }

    public static Uri specificRecordJoinWithSpecificParentMatch() {
        return mockUri(authority, specificRecordJoinWithSpecificParentMatch.toArray(new String[specificRecordJoinWithSpecificParentMatch.size()]));
    }

    private static Uri mockUri(String authority, String... pathSegments) {
        Uri uri = Mockito.mock(Uri.class);
        Mockito.when(uri.getAuthority()).thenReturn(authority);
        Mockito.when(uri.getLastPathSegment()).thenReturn(pathSegments[pathSegments.length - 1]);
        Mockito.when(uri.getPathSegments()).thenReturn(Lists.newArrayList(pathSegments));
        return uri;
    }
}
