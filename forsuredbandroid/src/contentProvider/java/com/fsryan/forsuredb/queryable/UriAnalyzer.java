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

    public static final String QUERY_PARAM_DISTINCT = "DISTINCT";
    public static final String QUERY_PARAM_UPSERT = "UPSERT";
    public static final String QUERY_PARAM_JOIN = "JOIN";
    public static final String QUERY_PARAM_LIMITS = "LIMITS";
    public static final String QUERY_PARAM_ORDERING = "ORDER";

    private static final Pattern ID_SELECTION_PATTERN = Pattern.compile("_id *(=|IS) *\\?");

    private static final char delimChar = ':';
    private static final char joinColumnMapDelim = '=';

    @NonNull
    private final Uri uri;

    public UriAnalyzer(@NonNull Uri uri) {
        this.uri = uri;
    }

    /**
     * <p>Because the {@link Uri} may be encoded with joins, a single {@link Uri} may include
     * references for other tables. This method splits them all up and returns a list of {@link Uri}
     * locators for all of the referenced tables--one entry for each
     * <p>A {@link Collections#singletonList(Object)} will be returned when there are no joins
     * <p>Note that this method <i>DOES NOT</i> preserve the order that the joins were added to the
     * {@link Uri}
     * @param uri the {@link Uri} to split into its constituent table locators
     * @return a list of all of the {@link Uri} locators for the tables referenced by the
     * {@link Uri} input or an empty list if the input is null
     */
    @NonNull
    public static List<Uri> tableLocatorsOf(@Nullable Uri uri) {
        if (uri == null) {
            return Collections.emptyList();
        }
        if (!isForJoin(uri)) {
            return Collections.singletonList(uri);
        }

        final Set<String> tableNames = new HashSet<>();
        tableNames.add(ForSureAndroidInfoFactory.inst().tableName(uri));
        for (FSJoin join : extractJoinsFrom(uri)) {
            tableNames.add(join.getChildTable());
            tableNames.add(join.getParentTable());
        }

        final List<Uri> ret = new ArrayList<>(tableNames.size());
        for (String tableName : tableNames) {
            ret.add(ForSureAndroidInfoFactory.inst().tableResource(tableName));
        }
        return ret;
    }

    /**
     * @param uri the {@link Uri} to check whether it is for an upsert query
     * @return false if the input is null or not for an upsert query, otherwise true
     */
    public static boolean isForUpsert(@Nullable Uri uri) {
        return uri != null && uri.getBooleanQueryParameter(QUERY_PARAM_UPSERT, false);
    }

    /**
     * @param uri the {@link Uri} to check whether it is for a join query
     * @return false if the input is null or not for a join query, otherwise true
     */
    public static boolean isForJoin(@Nullable Uri uri) {
        return uri != null && uri.getQueryParameter(QUERY_PARAM_JOIN) != null;
    }

    /**
     * @param uri the {@link Uri} to check whether it is for a distinct query
     * @return false if the input is null or not for a distinct query, otherwise true
     */
    public static boolean isForDistinctQuery(@Nullable Uri uri) {
        return uri != null && uri.getBooleanQueryParameter(QUERY_PARAM_DISTINCT, false);
    }

    /**
     * @param uri the {@link Uri} to check whether the query has been limited
     * @return false if the input is null or the query was not limited, otherwise true
     */
    public static boolean isLimited(@Nullable Uri uri) {
        return uri != null && uri.getQueryParameter(QUERY_PARAM_LIMITS) != null;
    }

    /**
     * @param uri the {@link Uri} to check whether the query has been ordered
     * @return false if the input is null or the query was not ordered, otherwise true
     */
    public static boolean isOrdered(@Nullable Uri uri) {
        return uri != null && uri.getQueryParameter(QUERY_PARAM_ORDERING) != null;
    }

    // TODO: this is probably not correct, but it doesn't seem broken for the way Uri is being used
    /**
     * <p>A {@link Uri} is considered to be a specific record {@link Uri} in the case that it:
     * <ul>
     *     <li>has less than 2 path segments</li>
     *     <li>has an odd number of path segments (such as parent/1/child)</li>
     * </ul>
     * @param uri The {@link Uri} to check
     * @return true if the Uri is a specific record Uri, false if not
     */
    public static boolean isSpecificRecordUri(@Nullable Uri uri) {
        if (uri == null) {
            return false;
        }

        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() < 2 || pathSegments.size() % 2 == 1) {
            return false;
        }

        return true;
    }

    /**
     * where type is {@link FSJoin.Type}, and everything else is a string
     * @param input the {@link FSJoin} source for the String representation
     * @return a stringified version of an {@link FSJoin} intended to be put into a Uri
     * @throws NullPointerException on null input
     * @see #destringifyFSJoin(String) for output format
     */
    @NonNull
    public static String stringify(@NonNull FSJoin input) {
        StringBuilder buf = new StringBuilder(input.getType().name()).append(delimChar)
                .append(input.getChildTable()).append(delimChar)
                .append(input.getParentTable()).append(delimChar);
        for (Map.Entry<String, String> entry : input.getChildToParentColumnMap().entrySet()) {
            buf.append(entry.getKey()).append(joinColumnMapDelim)
                    .append(entry.getValue()).append(delimChar);
        }
        return deleteFromEnd(buf, 1).toString();
    }

    /**
     * where count and offset are ints and isBottom is a boolean
     * @param input the {@link Limits} to stringify
     * @return a String representation of the {@link Limits} object input
     * @see #destringifyLimits(String) for the output format
     */
    @NonNull
    public static String stringify(@NonNull Limits input) {
        return Integer.toString(input.count()) + delimChar
                + Integer.toString(input.offset()) + delimChar
                + input.isBottom();
    }

    /**
     * @param input the {@link FSOrdering} to stringify
     * @return a String representation of a {@link FSOrdering}
     * @see #destringifyFSOrdering(String) for the output format
     */
    @NonNull
    public static String stringify(@NonNull FSOrdering input) {
        return input.table + delimChar + input.column + delimChar + input.direction;
    }

    /**
     * <p>format: type:child:parent:child_column=parent_column:child_column:parent_column...
     * @param fsJoinStr the String representation of an {@link FSJoin} to parse
     * @return an {@link FSJoin} built from the string input
     * @throws ParseException when input is not in correct format
     */
    @NonNull
    public static FSJoin destringifyFSJoin(@NonNull String fsJoinStr) throws ParseException {
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
            if (parentColumn.length() == 0) {
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
     * @throws ParseException when input is not in correct format
     */
    @NonNull
    public static Limits destringifyLimits(@NonNull String limitsStr) throws ParseException {
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
     * @throws ParseException when input is not in correct format
     */
    @NonNull
    public static FSOrdering destringifyFSOrdering(@NonNull String fsOrdering) throws ParseException {
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

    /**
     * @param uri the {@link Uri} from which to extract the encoded list of {@link FSOrdering}s
     * @return null whenever the input {@link Uri} is null--otherwise, the List of
     * {@link FSOrdering}s encoded in the input {@link Uri}
     * @throws RuntimeException when the {@link Uri} was encoded with an incorrectly-formatted
     * representation of a {@link FSOrdering}
     */
    @Nullable
    public static List<FSOrdering> extractOrderingsFrom(@Nullable Uri uri) {
        if (uri == null) {
            return null;
        }
        return new UriAnalyzer(uri).getOrderingsUnsafe();
    }

    /**
     * @param uri the {@link Uri} from which to extract the encoded list of {@link FSJoin}s
     * @return null whenever the input {@link Uri} is null--otherwise, the List of {@link FSJoin}s
     * encoded in the input {@link Uri}
     * @throws RuntimeException when the {@link Uri} was encoded with an incorrectly-formatted representation of a {@link FSJoin}
     */
    @Nullable
    public static List<FSJoin> extractJoinsFrom(@Nullable Uri uri) {
        if (uri == null) {
            return null;
        }
        return new UriAnalyzer(uri).getJoinsUnsafe();
    }

    /**
     * @return true if the {@link Uri} analyzed by this {@link UriAnalyzer} has at least one
     * {@link FSJoin} encoded, otherwise false
     */
    public boolean hasJoin() {
        return isForJoin(uri);
    }

    /**
     * @return true if the {@link Uri} analyzed by this {@link UriAnalyzer} has an {@link Limits}
     * encoded, otherwise false
     * @see #isLimited(Uri)
     */
    public boolean hasLimits() {
        return isLimited(uri);
    }

    /**
     * @return true if the {@link Uri} analyzed by this {@link UriAnalyzer} has at least one
     * {@link FSOrdering} encoded, otherwise false
     * @see #isOrdered(Uri)
     */
    public boolean hasOrdering() {
        return isOrdered(uri);
    }

    /**
     * @return true if the {@link Uri} analyzed by this {@link UriAnalyzer} is encoded as distinct,
     * otherwise false
     * @see #isForDistinctQuery(Uri)
     */
    public boolean isDistinct() {
        return isForDistinctQuery(uri);
    }

    /**
     * @return true if the {@link Uri} analyzed by this {@link UriAnalyzer} is encoded as an upsert,
     * otherwise false
     * @see #isForUpsert(Uri)
     */
    public boolean isUpsert() {
        return isForUpsert(uri);
    }

    /**
     * @return a list of {@link FSJoin} describing the joins encoded in the {@link Uri} analyzed by
     * this {@link UriAnalyzer} in order of their definition in the {@link Uri}
     * @throws ParseException when the {@link Uri} analyzed by this {@link UriAnalyzer} was encoded
     * with an incorrectly-formatted representation of a {@link FSJoin}
     * @see #getJoinsUnsafe() if you don't want to catch the {@link ParseException}
     */
    @NonNull
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

    /**
     * @return a list of {@link FSJoin} describing the joins encoded in the {@link Uri} analyzed by
     * this {@link UriAnalyzer} in order of their definition in the {@link Uri}
     * @see #getJoins() if you want to catch the {@link ParseException}
     */
    @NonNull
    public List<FSJoin> getJoinsUnsafe() {
        try {
            return getJoins();
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
    }

    /**
     * @return the {@link Limits} encoded in the {@link Uri} analyzed by this {@link UriAnalyzer} or
     * null if none
     * @throws ParseException when the {@link Uri} analyzed by this {@link UriAnalyzer}  was encoded
     * with an incorrectly-formatted representation of a {@link Limits}
     * @see #getLimitsUnsafe() if you do not want to catch the {@link ParseException}
     */
    @Nullable
    public Limits getLimits() throws ParseException {
        final String limitStr = uri.getQueryParameter(QUERY_PARAM_LIMITS);
        return limitStr == null ? null : destringifyLimits(limitStr);
    }

    /**
     * @return the {@link Limits} encoded in the {@link Uri} analyzed by this {@link UriAnalyzer} or
     * null if none
     * @throws RuntimeException when the wrapped parser throws a {@link ParseException}
     * @see #getLimits() if you want to catch the {@link ParseException}
     */
    @Nullable
    public Limits getLimitsUnsafe() {
        try {
            return getLimits();
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
    }

    /**
     * <p>The {@link Uri} may either specify a table, subset of records in a table, or a single
     * record of the table. This method ensures that any such encoding in the {@link Uri} is
     * properly added to the {@link FSSelection#where()} and {@link FSSelection#replacements()}
     * <i>on egress</i>. Without this, a RESTful schema for locating records would not be possible.
     * <p>Note that while this method does not throw any exceptions, whenever the
     * {@link FSSelection#limits()} method is called, a {@link RuntimeException} could occur on an
     * improperly encoded {@link Uri}.
     * @param currentSelection the currently-known WHERE clause
     * @param currentSelectionArgs the currently-known replacements for ? in the WHERE clause
     * @return and {@link FSSelection} that takes any encoded selection parameters into account when
     * locating records.
     */
    public FSSelection getSelection(final String currentSelection, final String[] currentSelectionArgs) {
        final boolean specificRecordUri = isSpecificRecordUri(uri);
        return new FSSelection() {

            String actualWhere = null;
            Object[] actualReplacements = null;

            @Override
            public String where() {
                if (actualWhere == null) {
                    actualWhere = specificRecordUri ? ensureIdInSelection(currentSelection) : currentSelection;
                }
                return actualWhere;
            }

            @Override
            public Object[] replacements() {
                if (actualReplacements == null) {
                    String[] toDeserialize = specificRecordUri
                            ? ensureIdInSelectionArgs(where(), currentSelectionArgs)
                            : currentSelectionArgs;
                    actualReplacements = ReplacementSerializer.deserializeAll(toDeserialize);
                }
                return actualReplacements;
            }

            @Override
            public Limits limits() {
                return getLimitsUnsafe();
            }
        };
    }

    /**
     * @return a list of the {@link FSOrdering} encoded in the {@link Uri} analyzed by this
     * {@link UriAnalyzer}
     * @throws ParseException when the {@link Uri} analyzed by this {@link UriAnalyzer} was encoded
     * with an incorrectly-formatted representation of a {@link FSOrdering}
     * @see #getOrderingsUnsafe() if you do not want to catch the {@link ParseException}
     */
    @NonNull
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

    /**
     * @return a list of the {@link FSOrdering} encoded in the {@link Uri} analyzed by this
     * {@link UriAnalyzer}
     * @throws RuntimeException when the {@link Uri} analyzed by this {@link UriAnalyzer} was
     * encoded with an incorrectly-formatted representation of a {@link FSOrdering}
     * @see #getOrderings() () if you do want to catch the {@link ParseException}
     */
    @NonNull
    public List<FSOrdering> getOrderingsUnsafe() {
        try {
            return getOrderings();
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }
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

        final int inputSize = selectionArgs == null ? 0 : selectionArgs.length;
        List<String> ret = new ArrayList<>(inputSize + 2);
        ret.add(0, "L");    // <-- prepend because the modified selection string specifies the _id selection first
        ret.add(1, uri.getLastPathSegment());
        if (inputSize > 0) {
            ret.addAll(Arrays.asList(selectionArgs));
        }
        return ret.toArray(new String[ret.size()]);
    }

    // The selectionArgs array contains two entries for each replacement. So if the number of
    // question marks is greater than half the number of selection args, then the selection was
    // modified to accommodate an id. Note that selectionArgs.length is always even or zero.
    private static boolean selectionWasModified(String selection, String[] selectionArgs) {
        final int qMarkCount = selection == null ? 0 : selection.replaceAll("[^?]", "").length();
        if (selectionArgs == null || selectionArgs.length == 0) {
            return qMarkCount == 1;
        }
        return qMarkCount > selectionArgs.length / 2;
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
