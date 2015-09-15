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

import com.forsuredb.api.FSProjection;
import com.forsuredb.api.FSQueryable;
import com.forsuredb.api.FSSelection;
import com.forsuredb.api.Retriever;

/*package*/ class ContentProviderQueryable implements FSQueryable<Uri, FSContentValues> {

    private final Context appContext;
    private final Uri resource;

    public ContentProviderQueryable(Context appContext, Uri resource) {
        this.appContext = appContext;
        this.resource = resource;
    }

    @Override
    public Uri insert(FSContentValues cv) {
        return appContext.getContentResolver().insert(resource, cv.getContentValues());
    }

    @Override
    public int update(FSContentValues cv, FSSelection selection) {
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return appContext.getContentResolver().update(resource, cv.getContentValues(), s, sArgs);
    }

    @Override
    public int delete(FSSelection selection) {
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return appContext.getContentResolver().delete(resource, s, sArgs);
    }

    @Override
    public Retriever query(FSProjection projection, FSSelection selection, String sortOrder) {
        final String[] p = projection == null ? null : projection.columns();
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return new FSCursor(appContext.getContentResolver().query(resource, p, s, sArgs, sortOrder));
    }
}
