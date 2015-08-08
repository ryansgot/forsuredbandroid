package com.forsuredb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/*package*/ class ContentProviderQueryable implements FSQueryable<Uri, ContentValues, Cursor> {

    private final Context appContext;
    private final Uri resource;

    public ContentProviderQueryable(Context appContext, Uri resource) {
        this.appContext = appContext;
        this.resource = resource;
    }

    @Override
    public Uri insert(ContentValues cv) {
        return appContext.getContentResolver()
                         .insert(resource, cv);
    }

    @Override
    public int update(ContentValues cv, FSSelection selection) {
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return appContext.getContentResolver()
                         .update(resource, cv, s, sArgs);
    }

    @Override
    public int delete(FSSelection selection) {
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return appContext.getContentResolver()
                         .delete(resource, s, sArgs);
    }

    @Override
    public Cursor query(FSProjection projection, FSSelection selection, String sortOrder) {
        final String[] p = projection == null ? null : projection.columns();
        final String s = selection == null ? null : selection.where();
        final String[] sArgs = selection == null ? null : selection.replacements();
        return appContext.getContentResolver()
                         .query(resource, p, s, sArgs, sortOrder);
    }
}
