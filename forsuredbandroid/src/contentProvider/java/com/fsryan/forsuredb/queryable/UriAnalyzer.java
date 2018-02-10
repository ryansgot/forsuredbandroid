package com.fsryan.forsuredb.queryable;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fsryan.forsuredb.ForSureAndroidInfoFactory;
import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Limits;
import com.fsryan.forsuredb.api.OrderBy;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class UriAnalyzer {

    public static String QUERY_PARAM_DISTINCT = "DISTINCT";
    public static String QUERY_PARAM_UPSERT = "UPSERT";
    public static String QUERY_PARAM_JOIN = "JOIN";
    public static String QUERY_PARAM_LIMITS = "LIMITS";
    public static String QUERY_PARAM_ORDERING = "ORDER";

    private static final Pattern ID_SELECTION_PATTERN = Pattern.compile("_id *(=|IS) *\\?");

    private static final char delimChar = ':';
    private static final char joinColumnMapDelim = '=';

    @NonNull
    private final Uri uri;

    public UriAnalyzer(@NonNull Uri uri) {
        this.uri = uri;
    }

    /**
     * <p>format: type:child:parent:child_column=parent_column:child_column:parent_column...
     * where type is {@link FSJoin.Type}, and everything else is a string
     * @param input the {@link FSJoin} source for the String representation
     * @return a stringified version of an {@link FSJoin} intended to be put into a Uri
     * @throws NullPointerException on null input
     */
    public static String stringify(@NonNull FSJoin input) {
        if (input == null) {
            throw new NullPointerException("cannot stringify null");
        }

        StringBuilder buf = new StringBuilder(input.getType().name()).append(delimChar)
                .append(input.getChildTable()).append(delimChar)
                .append(input.getParentTable()).append(delimChar);
        for (Map.Entry<String, String> entry : input.getChildToParentColumnMap().entrySet()) {
            buf.append(entry.getKey()).append(joinColumnMapDelim)
                    .append(entry.getValue()).append(delimChar);
        }
        return deleteFromEnd(buf, 1).toString();
    }

    public static List<Uri> tableLocatorsOf(@Nullable Uri uri) {
        if (uri == null) {
            return Collections.emptyList();
        }

        List<FSJoin> joins = extractJoinsUnsafe(uri);
        Set<String> tableNames = new HashSet<>();
        tableNames.add(ForSureAndroidInfoFactory.inst().tableName(uri));
        for (FSJoin join : joins == null ? Collections.<FSJoin>emptyList() : joins) {
            tableNames.add(join.getChildTable());
            tableNames.add(join.getParentTable());
        }

        List<Uri> ret = new ArrayList<>(tableNames.size());
        for (String tableName : tableNames) {
            ret.add(ForSureAndroidInfoFactory.inst().tableResource(tableName));
        }
        return ret;
    }

    public static boolean isForUpsert(@Nullable Uri uri) {
        return uri != null && uri.getBooleanQueryParameter(QUERY_PARAM_UPSERT, false);
    }

    public static boolean isForJoin(@Nullable Uri uri) {
        return uri != null && uri.getQueryParameter(QUERY_PARAM_JOIN) != null;
    }

    public static boolean isForDistinctQuery(@Nullable Uri uri) {
        return uri != null && uri.getBooleanQueryParameter(QUERY_PARAM_DISTINCT, false);
    }

    /**
     * where count and offset are ints and isBottom is a boolean
     * @param input the {@link Limits} to stringify
     * @return a String representation of the {@link Limits} object input
     * @throws NullPointerException on null input
     * @see #destringifyLimits(String) for the output format
     */
    public static String stringify(@NonNull Limits input) {
        if (input == null) {
            throw new NullPointerException("cannot stringify null");
        }

        return Integer.toString(input.count()) + delimChar
                + Integer.toString(input.offset()) + delimChar
                + input.isBottom();
    }

    /**
     * @param input the {@link FSOrdering} to stringify
     * @return a String representation of a {@link FSOrdering}
     * @throws NullPointerException on null input
     * @see #destringifyFSOrdering(String) for the output format
     */
    public static String stringify(@NonNull FSOrdering input) {
        if (input == null) {
            throw new NullPointerException("cannot stringify null");
        }

        return input.table + delimChar + input.column + delimChar + input.direction;
    }

    /**
     * @param fsJoinStr the String representation of an {@link FSJoin} to parse
     * @return an {@link FSJoin} built from the string input
     * @throws java.text.ParseException
     * @throws NullPointerException on null input
     * @see #stringify(FSJoin) for the output format
     */
    public static FSJoin destringifyFSJoin(@NonNull String fsJoinStr) throws ParseException {
        if (fsJoinStr == null) {
            throw new NullPointerException("cannot parse null");
        }

        int delim = fsJoinStr.indexOf(delimChar);
        if (delim < 0) {
            throw new ParseException(delimChar + " not found: " + fsJoinStr, 0);
        }

        FSJoin.Type type = null;
        try {
            type = FSJoin.Type.valueOf(fsJoinStr.substring(0, delim));
        } catch (IllegalArgumentException iae) {
            throw new ParseException("expected one of " + fsJoinNames(), 0);
        }

        int start = delim + 1;
        delim = fsJoinStr.indexOf(delimChar, start);
        if (delim < 0) {
            throw new ParseException(delimChar + " not found: " + fsJoinStr, start);
        }
        final String childTable = fsJoinStr.substring(start, delim);
        if (childTable.length() < 1) {
            throw new ParseException("child table must be nonempty: " + fsJoinStr, start);
        }


        String parentTable = null;
        start = delim + 1;
        delim = fsJoinStr.indexOf(delimChar, start);
        if (type == FSJoin.Type.NATURAL && delim < 0) {
            parentTable = fsJoinStr.substring(start);
            if (parentTable.length() < 1) {
                throw new ParseException("parent table must be nonempty: " + fsJoinStr, start);
            }
            return new FSJoin(type, parentTable, childTable, Collections.<String, String>emptyMap());
        } else {
            parentTable = fsJoinStr.substring(start, delim);
        }

        if (parentTable.length() < 1) {
            throw new ParseException("parent table must be nonempty: " + fsJoinStr, start);
        }

        start = delim + 1;
        String[] splitMap = fsJoinStr.substring(start).split(Character.toString(delimChar));
        if (splitMap.length < 1) {
            throw new ParseException("expecting child and parent columns separated by =: " + fsJoinStr, start);
        }
        Map<String, String> childToParentColumnMap = new HashMap<>(splitMap.length / 2);
        for (String childParent : splitMap) {
            delim = childParent.indexOf(joinColumnMapDelim);
            if (delim < 0) {
                throw new ParseException("expecting parent column for each child column: " + fsJoinStr, start);
            }
            final String childColumn = childParent.substring(0, delim);
            if (childColumn.length() == 0) {
                throw new ParseException("expecting child column: " + fsJoinStr, start);
            }
            final String parentColumn = childParent.substring(delim + 1);
            if (childColumn.length() == 0) {
                throw new ParseException("expecting parent column: " + fsJoinStr, start);
            }
            childToParentColumnMap.put(childColumn, parentColumn);
        }

        return new FSJoin(type, parentTable, childTable, childToParentColumnMap);
    }

    /**
     * <p>format: count:offset:isBottom
     * where count and offset are ints and isBottom is a boolean
     * @param limitsStr the String representation of an {@link Limits} to parse
     * @return the {@link Limits} object represented by the input String
     * @throws ParseException
     * @throws NullPointerException on null input
     */
    public static Limits destringifyLimits(@NonNull String limitsStr) throws ParseException {
        if (limitsStr == null) {
            throw new NullPointerException("cannot parse null");
        }

        int count = 0;
        int offset = 0;

        int start = 0;
        int delim = limitsStr.indexOf(delimChar);
        if (delim < 0) {
            throw new ParseException(delimChar + " expected: " + limitsStr, start);
        } else if (delim > 0) {
            final String countStr = limitsStr.substring(start, delim);
            if (!countStr.isEmpty()) {  // <-- empty means 0
                try {
                    count = Integer.parseInt(countStr);
                } catch (NumberFormatException nfe) {
                    throw new ParseException("Bad Number format for count: " + limitsStr, start);
                }
            }
        }

        start = delim + 1;
        delim = limitsStr.indexOf(delimChar, start);
        if (delim < 0) {
            throw new ParseException(delimChar + " expected: " + limitsStr, start);
        } else if (delim > 0) {
            final String offsetStr = limitsStr.substring(start, delim);
            if (!offsetStr.isEmpty()) { // <-- empty means 0
                try {
                    offset = Integer.parseInt(offsetStr);
                } catch (NumberFormatException nfe) {
                    throw new ParseException("Bad Number format for offset: " + limitsStr, start);
                }
            }
        }

        start = delim + 1;
        return new LimitsFromUri(count, offset, Boolean.parseBoolean(limitsStr.substring(start)));
    }

    /**
     * <p>format: table:column:direction
     * where table and column are Strings and direction is an int and either
     * {@link OrderBy#ORDER_ASC} or {@link OrderBy#ORDER_DESC}
     * @param fsOrdering the String representation of the {@link FSOrdering} to destringify
     * @return the {@link FSOrdering} represented by the input string
     * @throws ParseException
     * @throws NullPointerException on null input
     */
    public static FSOrdering destringifyFSOrdering(@Nullable String fsOrdering) throws ParseException {
        if (fsOrdering == null) {
            return null;
        }

        String[] split = fsOrdering.split(Character.toString(delimChar));
        if (split.length != 3) {
            throw new ParseException("expected 3 fields, got " + split.length + ": " + fsOrdering, 0);
        }

        final String table = split[0];
        if (table.isEmpty()) {
            throw new ParseException("Cannot have empty table name: " + fsOrdering, 0);
        }

        final String column = split[1];
        if (column.isEmpty()) {
            throw new ParseException("Cannot have empty column name: " + fsOrdering, table.length() + 1);
        }

        final String orderStr = split[2];
        int order = OrderBy.ORDER_ASC;
        if (!orderStr.isEmpty()) {
            try {
                order = Integer.parseInt(orderStr);
            } catch (NumberFormatException nfe) {
                throw new ParseException(
                        "Order represented as an int, acceptable values are " + OrderBy.ORDER_ASC + " and " + OrderBy.ORDER_DESC + ": " + fsOrdering,
                        table.length() + column.length() + 2
                );
            }
        }
        if (order != OrderBy.ORDER_ASC && order != OrderBy.ORDER_DESC) {
            throw new ParseException(
                    "acceptable order values are " + OrderBy.ORDER_ASC + " and " + OrderBy.ORDER_DESC + ": ",
                    table.length() + column.length() + 2
            );
        }

        return new FSOrdering(table, column, order);
    }

    public static List<FSOrdering> extractOrderingsUnsafe(Uri uri) {
        if (uri == null) {
            return null;
        }

        List<FSOrdering> ret = new ArrayList<>(4);
        for (String queryParam : uri.getQueryParameters(QUERY_PARAM_ORDERING)) {
            try {
                ret.add(destringifyFSOrdering(queryParam));
            } catch (ParseException pe) {
                throw new RuntimeException(pe);
            }
        }

        return ret;
    }

    public static List<FSJoin> extractJoinsUnsafe(Uri uri) {
        if (uri == null) {
            return null;
        }

        List<FSJoin> ret = new ArrayList<>(4);
        for (String queryParam : uri.getQueryParameters(QUERY_PARAM_JOIN)) {
            try {
                ret.add(destringifyFSJoin(queryParam));
            } catch (ParseException pe) {
                throw new RuntimeException(pe);
            }
        }

        return ret;
    }

    public boolean hasJoin() {
        return isForJoin(uri);
    }

    public boolean hasLimits() {
        return uri.getQueryParameter(QUERY_PARAM_LIMITS) != null;
    }

    public boolean hasOrdering() {
        return uri.getQueryParameter(QUERY_PARAM_ORDERING) != null;
    }

    public boolean isDistinct() {
        return isForDistinctQuery(uri);
    }

    public boolean isUpsert() {
        return isForUpsert(uri);
    }

    public List<FSJoin> getJoins() throws ParseException {
        List<String> joinParams = uri.getQueryParameters(QUERY_PARAM_JOIN);
        if (joinParams == null || joinParams.isEmpty()) {
            return Collections.emptyList();
        }

        List<FSJoin> ret = new ArrayList<>(joinParams.size());
        for (String fsJoinStr : joinParams) {
            ret.add(destringifyFSJoin(fsJoinStr));
        }

        return ret;
    }

    public List<FSJoin> getJoinsUnsafe() {
        return extractJoinsUnsafe(uri);
    }

    public Limits getLimits() throws ParseException {
        final String limitStr = uri.getQueryParameter(QUERY_PARAM_LIMITS);
        return limitStr == null ? null : destringifyLimits(limitStr);
    }

    public FSSelection getSelection(final String currentSelection, final String[] currentSelectionArgs) {
        final boolean specificRecordUri = isSpecificRecordUri(uri);
        return new FSSelection() {
            @Override
            public String where() {
                return specificRecordUri ? ensureIdInSelection(currentSelection) : currentSelection;
            }

            @Override
            public String[] replacements() {
                return specificRecordUri
                        ? ensureIdInSelectionArgs(where(), currentSelectionArgs)
                        : currentSelectionArgs;
            }

            @Override
            public Limits limits() {
                try {
                    return getLimits();
                } catch (ParseException pe) {
                    throw new RuntimeException(pe);
                }
            }
        };
    }

    public List<FSOrdering> getOrderings() throws ParseException {
        List<String> orderingParams = uri.getQueryParameters(QUERY_PARAM_ORDERING);
        if (orderingParams == null || orderingParams.isEmpty()) {
            return Collections.emptyList();
        }

        List<FSOrdering> ret = new ArrayList<>(orderingParams.size());
        for (String orderingStr : orderingParams) {
            ret.add(destringifyFSOrdering(orderingStr));
        }

        return ret;
    }

    public List<FSOrdering> getOrderingsUnsafe() {
        return extractOrderingsUnsafe(uri);
    }

    // inner access
    String ensureIdInSelection(String selection) {
        if (selection == null || selection.isEmpty()) {
            return "_id = ?";
        }

        if (!ID_SELECTION_PATTERN.matcher(selection).find()) {
            return "_id = ? AND (" + selection + ")";    // <-- insert the _id selection if it doesn't exist
        }
        return selection;
    }

    // inner access
    String[] ensureIdInSelectionArgs(String where, String[] selectionArgs) {
        if (!selectionWasModified(where, selectionArgs)) {
            return selectionArgs;
        }

        final List<String> selectionArgList = selectionArgs == null || selectionArgs.length == 0
                ? new ArrayList<String>(1)
                : new ArrayList<>(Arrays.asList(selectionArgs));
        selectionArgList.add(0, uri.getLastPathSegment());  // <-- prepend because the modified selection string specifies the _id selection first
        return selectionArgList.toArray(new String[selectionArgList.size()]);
    }

    // If the selection was modified, then the number of ? in the selection will be one more than the length of selectionArgs
    private static boolean selectionWasModified(String selection, String[] selectionArgs) {
        final int qMarks = selection == null ? 0 : selection.replaceAll("[^?]", "").length();
        if (selectionArgs == null || selectionArgs.length == 0) {
            return qMarks == 1;
        }
        return qMarks == selectionArgs.length + 1;
    }

    private static StringBuilder deleteFromEnd(StringBuilder buf, int count) {
        return buf.delete(buf.length() - count, buf.length());
    }

    private static String fsJoinNames() {
        FSJoin.Type[] types = FSJoin.Type.values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].name();
        }
        return Arrays.toString(names);
    }

    /**
     * TODO: this is probably not correct, but it doesn't seem broken for the way {@link Uri} is being used
     * <p>
     *     A {@link Uri} is considered to be a specific record {@link Uri} in the case that it:
     *     <ul>
     *         <li>has less than 2 path segments</li>
     *         <li>has an odd number of path segments (such as parent/1/child)</li>
     *     </ul>
     * </p>
     * @param uri The {@link Uri} to check
     * @return true if the Uri is a specific record Uri, false if not
     */
    public static boolean isSpecificRecordUri(Uri uri) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() < 2 || pathSegments.size() % 2 == 1) {
            return false;
        }

        return true;
    }

    private static class LimitsFromUri implements Limits {

        private final int count;
        private final int offset;
        private final boolean isBottom;

        LimitsFromUri(int count, int offset, boolean isBottom) {
            this.count = count;
            this.offset = offset;
            this.isBottom = isBottom;
        }

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
            return isBottom;
        }
    }
}
