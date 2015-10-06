/*
   forsuredb, an object relational mapping tool

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
