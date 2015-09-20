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
package com.forsuredb;

import android.content.Context;
import android.net.Uri;

import com.forsuredb.api.FSQueryable;
import com.forsuredb.api.ForSureInfoFactory;

public class ForSureAndroidInfoFactory implements ForSureInfoFactory<Uri, FSContentValues> {

    private static final String URI_PREFIX = "content://com.forsuredb.testapp.content";

    private final Context appContext;

    public ForSureAndroidInfoFactory(Context appContext) {
        this.appContext = appContext.getApplicationContext();
    }

    @Override
    public FSQueryable<Uri, FSContentValues> createQueryable(Uri resource) {
        return new ContentProviderQueryable(appContext, resource);
    }

    @Override
    public FSContentValues createRecordContainer() {
        return FSContentValues.getNew();
    }

    @Override
    public Uri tableResource(String tableName) {
        return Uri.parse(URI_PREFIX + "/" + tableName);
    }

    @Override
    public String tableName(Uri uri) {
        return isSingleRecord(uri) ? uri.getPathSegments().get(uri.getPathSegments().size() - 2) : uri.getLastPathSegment();
    }

    private boolean isSingleRecord(Uri uri) {
        try {
            Long.parseLong(uri.getLastPathSegment());
            return true;
        } catch (Exception e) {
            // do nothing
        }
        return false;
    }
}
