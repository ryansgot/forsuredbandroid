package com.fsryan.forsuredb.queryable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fsryan.forsuredb.api.FSProjection;
import com.fsryan.forsuredb.api.sqlgeneration.Sql;

import java.util.ArrayList;
import java.util.List;

/*package*/ class ProjectionHelper {

    public static boolean isDistinct(@NonNull Iterable<FSProjection> projections) {
        for (FSProjection projection : projections) {
            if (projection.isDistinct()) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    public static String[] formatProjection(@Nullable FSProjection projection, @Nullable FSProjection... projections) {
        final int size = (projection == null ? 0 : 1) + (projections == null ? 0 : projections.length);
        if (size == 0) {
            return new String[0];
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

    @NonNull
    public static String[] formatProjection(@Nullable List<FSProjection> projections) {
        if (projections == null || projections.size() == 0) {
            return new String[0];
        }
        List<String> formattedProjectionList = new ArrayList<>();
        for (FSProjection projection : projections) {
            appendProjectionToList(formattedProjectionList, projection);
        }
        return formattedProjectionList.toArray(new String[formattedProjectionList.size()]);
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
