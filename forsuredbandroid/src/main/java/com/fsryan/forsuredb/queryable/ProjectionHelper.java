package com.fsryan.forsuredb.queryable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*package*/ class ProjectionHelper {

    public static boolean isDistinct(@NonNull Iterable<FSProjection> projections) {
        for (FSProjection projection : projections) {
            if (projection.isDistinct()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static String[] formatProjection(@Nullable FSProjection projection, @Nullable FSProjection... projections) {
        final int size = (projection == null ? 0 : 1) + (projections == null ? 0 : projections.length);
        if (size == 0) {
            return null;
        }

        List<FSProjection> ps = new ArrayList<>(size);
        if (projection != null) {
            ps.add(projection);
        }
        if (projections != null) {
            for (FSProjection p : projections) {
                ps.add(p);
            }
        }

        return formatProjection(ps);
    }

    @Nullable
    public static String[] formatProjection(@Nullable List<FSProjection> projections) {
        if (projections == null || projections.size() == 0) {
            return null;
        }
        List<String> formattedProjectionList = new ArrayList<>();
        for (FSProjection projection : projections) {
            appendProjectionToList(formattedProjectionList, projection);
        }
        return formattedProjectionList.toArray(new String[formattedProjectionList.size()]);
    }

    public static FSProjection toFSProjection(@NonNull final String table,
                                              final boolean distinct,
                                              @Nullable final String[] formattedProjection) {
        final String[] actualProjection = new String[formattedProjection == null ? 0 : formattedProjection.length];
        for (int i = 0; i < (formattedProjection == null ? 0 : formattedProjection.length); i++) {
            int delim = formattedProjection[i].indexOf('.');
            if (delim < 0 || delim == formattedProjection[i].length() - 1 || delim == 0) {
                throw new IllegalArgumentException("input projection must be formatted (include a . that is not the first or final character): " + Arrays.toString(formattedProjection));
            }

            final String cutTable = formattedProjection[i].substring(delim + 1);
            delim = cutTable.indexOf(' ');
            if (delim == 0) {
                throw new IllegalArgumentException("input projection cannot have space following dot: " + formattedProjection[i]);
            }
            actualProjection[i] = delim < 0 ? cutTable : cutTable.substring(0, delim);
        }
        return new FSProjection() {
            @Override
            public String tableName() {
                return table;
            }

            @Override
            public String[] columns() {
                return actualProjection.length == 0 ? null : actualProjection;
            }

            @Override
            public boolean isDistinct() {
                return distinct;
            }
        };
    }

    public static List<FSProjection> toFSProjections(final boolean distinct, String[] formattedProjection) {
        Map<String, List<String>> tableToProjectionMap = new HashMap<>();
        for (String p : formattedProjection) {
            int delim = p.indexOf('.');
            if (delim < 0 || delim == p.length() - 1 || delim == 0) {
                throw new IllegalArgumentException("input projection must be formatted (include a . that is not the first or final character): " + p);
            }

            final String table = p.substring(0, delim);
            final String cutTable = p.substring(delim + 1);
            delim = cutTable.indexOf(' ');
            if (delim == 0) {
                throw new IllegalArgumentException("input projection cannot have space following dot: " + p);
            }
            final String column = delim < 0 ? cutTable : cutTable.substring(0, delim);
            List<String> pList = tableToProjectionMap.get(table);
            if (pList == null) {
                pList = new ArrayList<>();
                tableToProjectionMap.put(table, pList);
            }
            pList.add(column);
        }

        List<FSProjection> ret = new ArrayList<>(tableToProjectionMap.size());
        for (final Map.Entry<String, List<String>> entry : tableToProjectionMap.entrySet()) {
            ret.add(new FSProjection() {
                @Override
                public String tableName() {
                    return entry.getKey();
                }

                @Override
                public String[] columns() {
                    return entry.getValue().toArray(new String[0]);
                }

                @Override
                public boolean isDistinct() {
                    return distinct;
                }
            });
        }
        return ret;
    }

    private static void appendProjectionToList(List<String> listToAddTo, FSProjection projection) {
        if (projection == null || projection.columns() == null || projection.columns().length == 0) {
            return;
        }

        for (String column : projection.columns()) {
            listToAddTo.add(unambiguouslyAliasColumn(projection.tableName(), column));
        }
    }

    // TODO: move this method to forsuredb sqlitelib
    /**
     * The following code in SQLiteCursor.getColumnIndex forces us to disambiguate columns without using the table.column
     * notation
     * Hack according to bug 903852
     * final int periodIndex = columnName.lastIndexOf('.');
     * if (periodIndex != -1) {
     * Exception e = new Exception();
     * Log.e(TAG, "requesting column name with table name -- " + columnName, e);
     * columnName = columnName.substring(periodIndex + 1);
     * }
     * @param tableName
     * @param columnName
     * @return
     */
    private static String unambiguouslyAliasColumn(String tableName, String columnName) {
        final String unambiguousName = Sql.generator().unambiguousColumn(tableName, columnName);
        final String retrievalName = Sql.generator().unambiguousRetrievalColumn(tableName, columnName);
        return unambiguousName + " AS " + retrievalName;
    }
}
