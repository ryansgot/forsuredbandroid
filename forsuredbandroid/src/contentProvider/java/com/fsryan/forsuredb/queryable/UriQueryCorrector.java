package com.fsryan.forsuredb.queryable;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fsryan.forsuredb.ForSureAndroidInfoFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.fsryan.forsuredb.queryable.UriJoinTranslator.joinStringFrom;
import static com.fsryan.forsuredb.queryable.UriEvaluator.isSpecificRecordUri;
import static com.fsryan.forsuredb.queryable.UriEvaluator.limitFrom;
import static com.fsryan.forsuredb.queryable.UriEvaluator.offsetFrom;
import static com.fsryan.forsuredb.queryable.UriEvaluator.offsetFromLast;
import static com.fsryan.forsuredb.queryable.UriEvaluator.orderingFrom;

/*package*/ class UriQueryCorrector extends QueryCorrector {

    private static final String ID_SELECTION = "_id = ?";
    private static final Pattern ID_SELECTION_PATTERN = Pattern.compile("_id *(=|IS) *\\?");


    public UriQueryCorrector(@NonNull Uri uri, @Nullable String where, @Nullable String[] whereArgs) {
        this(
                uri,
                where == null ? "" : where,
                whereArgs == null ? new String[0] : whereArgs,
                orderingFrom(uri),
                offsetFrom(uri),
                limitFrom(uri),
                offsetFromLast(uri)
        );
    }

    /*package*/ UriQueryCorrector(@NonNull Uri uri, @NonNull String where, @NonNull String[] selectionArgs, @NonNull String orderBy, int offset, int limit, boolean findingLast) {
        super(
                ForSureAndroidInfoFactory.inst().tableName(uri),
                joinStringFrom(uri),
                isSpecificRecordUri(uri) ? ensureIdInSelection(where) : where,
                isSpecificRecordUri(uri)
                        ? ensureIdInSelectionArgs(uri, isSpecificRecordUri(uri) ? ensureIdInSelection(where)
                        : where, selectionArgs) : selectionArgs,
                orderBy,
                offset,
                limit,
                findingLast
        );
    }

    private static String ensureIdInSelection(@NonNull String selection) {
        if (selection.isEmpty()) {
            return ID_SELECTION;
        }

        if (!ID_SELECTION_PATTERN.matcher(selection).find()) {
            return ID_SELECTION + " AND (" + selection + ")";    // <-- insert the _id selection if it doesn't exist
        }
        return selection;
    }

    private static String[] ensureIdInSelectionArgs(@NonNull Uri uri, @NonNull String where, @NonNull String[] selectionArgs) {
        if (!selectionWasModified(where, selectionArgs)) {
            return selectionArgs;
        }

        final List<String> selectionArgList = new ArrayList<>(Arrays.asList(selectionArgs));
        selectionArgList.add(0, uri.getLastPathSegment());  // <-- prepend because the modified selection string specifies the _id selection first
        return selectionArgList.toArray(new String[selectionArgList.size()]);
    }

    // If the selection was modified, then the number of ? in the selection will be one more than the length of selectionArgs
    private static boolean selectionWasModified(@NonNull String selection, @NonNull String[] selectionArgs) {
        final int qMarkOccurrences = selection.replaceAll("[^?]", "").length();
        return qMarkOccurrences == selectionArgs.length + 1;
    }
}
