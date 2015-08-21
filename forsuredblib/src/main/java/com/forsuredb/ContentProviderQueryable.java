package com.forsuredb;

import android.content.Context;
import android.net.Uri;

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
    public Retriever query(com.forsuredb.api.FSProjection projection, FSSelection selection, String sortOrder) {
        final String[] p = projection == null ? null : projection.columns();
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return new FSCursor(appContext.getContentResolver().query(resource, p, s, sArgs, sortOrder));
    }
}
