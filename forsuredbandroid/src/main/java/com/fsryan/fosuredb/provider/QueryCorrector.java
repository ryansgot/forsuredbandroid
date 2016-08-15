/*
   forsuredbandroid, an android companion to the forsuredb project

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.fsryan.fosuredb.provider;

import android.net.Uri;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>
 *     Helps ensure properly formatted queries based upon the {@link Uri uri}, selection String,
 *     and selectionArgs passed into the constructor.
 * </p>
 * @author Ryan Scott
 */
public class QueryCorrector {

    private static final String ID_SELECTION = "_id = ?";
    private static final Pattern ID_SELECTION_PATTERN = Pattern.compile("_id *(=|IS) *\\?");

    private final String selection;
    private final String[] selectionArgs;

    public QueryCorrector(Uri uri, String selection, String[] selectionArgs) {
        final boolean isSingleRecordQuery = UriEvaluator.isSpecificRecordUri(uri);
        this.selection = isSingleRecordQuery ? ensureIdInSelection(selection) : selection;
        this.selectionArgs = isSingleRecordQuery ? ensureIdInSelectionArgs(uri, selectionArgs) : selectionArgs;
    }

    public String getSelection() {
        return selection;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    private String ensureIdInSelection(String selection) {
        if (Strings.isNullOrEmpty(selection)) {
            return ID_SELECTION;
        }

        if (!ID_SELECTION_PATTERN.matcher(selection).find()) {
            return ID_SELECTION + " AND (" + selection + ")";    // <-- insert the _id selection if it doesn't exist
        }
        return selection;
    }

    private String[] ensureIdInSelectionArgs(Uri uri, String[] selectionArgs) {
        if (!selectionWasModified(selection, selectionArgs)) {
            return selectionArgs;
        }

        final List<String> selectionArgList = selectionArgs == null ? new ArrayList<String>() : Lists.newArrayList(selectionArgs);
        selectionArgList.add(0, uri.getLastPathSegment());  // <-- prepend because the modified selection string specifies the _id selection first
        return selectionArgList.toArray(new String[selectionArgList.size()]);
    }

    // If the selection was modified, then the number of ? in the selection will be one more than the length of selectionArgs
    private boolean selectionWasModified(String selection, String[] selectionArgs) {
        final int qMarkOccurrences = occurrencesOf('?', selection);
        return (selectionArgs == null && qMarkOccurrences != 0) || qMarkOccurrences == selectionArgs.length + 1;
    }

    /**
     * <p>
     *     an approximation of the one available in the Apache Commons library's StringUtils
     *     countMatches method geared towards counts of characters
     * </p>
     * @param c character to count
     * @param str String in which to count characters
     * @return number of times c appears in str
     */
    private int occurrencesOf(char c, String str) {
        if (str == null) {
            return 0;
        }
        return str.replaceAll("[^" + c + "]", "").length();
    }
}
