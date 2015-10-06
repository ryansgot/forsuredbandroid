package com.forsuredb.provider;

import android.net.Uri;

/**
 * <p>
 *     A utility class to provide information about {@link Uri}s used to describe either tables
 *     or records in tables.
 * </p>
 */
public class UriEvaluator {

    /**
     * <p>
     *     A {@link Uri} is considered to be a specific record {@link Uri} in the case that it:
     *     <ul>
     *         <li>has less than 2 path segments</li>
     *         <li>has an odd number of path segments (such as parent/1/child)</li>
     *         <li>has a wildcard ({@link UriJoiner#WILDCARD}) as one of its
     *         odd-indexed path segments (such as parent/{@code*}/child/1)</li>
     *     </ul>
     * </p>
     * @param uri The {@link Uri} to check
     * @return true if the Uri is a specific record Uri, false if not
     */
    public static boolean isSpecificRecordUri(Uri uri) {
        String[] pathSegments = uri.getPathSegments().toArray(new String[uri.getPathSegments().size()]);
        if (pathSegments.length < 2 || pathSegments.length % 2 == 1) {
            return false;
        }

        for (int index = 1 ; index < pathSegments.length; index += 2) {
            if (pathSegments[index].equals(UriJoiner.WILDCARD)) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     *     A {@link Uri} is determined to be a join Uri if it has more than two path segments.
     * </p>
     * @param uri The {@link Uri} to check for whether it is a join Uri
     * @return true if the Uri is a join Uri, false otherwise
     */
    public static boolean isJoinUri(Uri uri) {
        return uri.getPathSegments().size() > 2;
    }
}
