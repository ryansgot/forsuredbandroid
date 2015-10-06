package com.forsuredb.provider;

import android.net.Uri;

public class UriJoiner {

    public static final String WILDCARD = "*";

    public static Uri join(Uri parentUri, Uri childUri) {
        Uri.Builder ub = new Uri.Builder().scheme(parentUri.getScheme()).authority(parentUri.getAuthority());
        appendPathSegmentsTo(ub, parentUri);
        appendPathSegmentsTo(ub, childUri);
        return ub.build();
    }

    private static void appendPathSegmentsTo(Uri.Builder ub, Uri uri) {
        for (String pathSegment : uri.getPathSegments()) {
            ub.appendPath(pathSegment);
        }
        if (uri.getPathSegments().size() % 2 == 1) {
            ub.appendPath(WILDCARD);
        }
    }

}
