package com.forsuredb.testapp.contentprovider;

import android.net.Uri;

import com.forsuredb.ForSure;
import com.forsuredb.table.FSTableDescriber;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*package*/ class ContentProviderHelper {

    public static FSTableDescriber resolveUri(Uri uri) {
        final List<String> segments = uri.getPathSegments();
        try {
            Long.parseLong(uri.getLastPathSegment());
            return ForSure.getInstance().getTable(segments.get(segments.size() - 2));
        } catch (NumberFormatException nfe) {
        }
        return ForSure.getInstance().getTable(uri.getLastPathSegment());
    }

    public static boolean isSingleRecord(Uri uri) {
        try {
            Long.parseLong(uri.getLastPathSegment());
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    // TODO(ryan): this is brittle and depends upon SQL query style
    public static String ensureIdInSelection(String selection) {
        if (Strings.isNullOrEmpty(selection)) {
            return "_id = ?";
        }
        if (!selection.contains("_id = ?")) {
            return selection;
        }
        return selection + " AND " + "_id = ?";
    }

    public static String[] ensureIdInSelectionArgs(Uri uri, String selection, String[] selectionArgs) {
        if (selectionWasModified(selection, selectionArgs)) {
            final List<String> selectionArgsList = selectionArgs == null ? new ArrayList<String>() : Lists.newArrayList(Arrays.asList(selectionArgs));
            selectionArgsList.add(uri.getLastPathSegment());
            return selectionArgsList.toArray(new String[selectionArgsList.size()]);
        }
        return selectionArgs;
    }

    private static boolean selectionWasModified(String selection, String[] selectionArgs) {
        final int qMarkOccurrences = occurrencesOf('?', selection);
        return (selectionArgs == null && qMarkOccurrences != 0) || qMarkOccurrences == selectionArgs.length + 1;
    }

    /**
     * <p>
     *     Return the count of occurrences of a character in a string. This sucks.
     *     The reason this is here rather than using Apache Commons StringUtil is that it's currently the only call to a method in
     *     the Apache Commons library that we need, and I don't want to pull in the whole library just for one method call.
     * </p>
     * @param charToCount
     * @param string
     * @return
     */
    private static int occurrencesOf(char charToCount, String string) {
        if (string == null) {
            return 0;
        }
        return string.replaceAll("[^" + charToCount + "]", "").length();
    }
}
