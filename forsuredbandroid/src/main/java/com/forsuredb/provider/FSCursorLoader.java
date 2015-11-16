package com.forsuredb.provider;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSSaveApi;
import com.forsuredb.api.Finder;
import com.forsuredb.api.Resolver;
import com.forsuredb.cursor.FSCursor;

import java.util.List;

public class FSCursorLoader<G extends FSGetApi, S extends FSSaveApi<Uri>, F extends Finder<Uri, G, S, F>> extends AsyncTaskLoader<FSCursor> {

    private FSCursor mCursor;
    private Resolver<Uri, G, S, F> resolver;
    private List<Uri> tableUris;
    private MultiTableObserver mObserver;

    public FSCursorLoader(Context context, Resolver<Uri, G, S, F> resolver) {
        super(context);
        this.resolver = resolver;
        tableUris = UriEvaluator.tableReferences(resolver.currentLocator());
    }

    @Override
    public FSCursor loadInBackground() {
        if (mObserver == null) {
            mObserver = new MultiTableObserver();
        }

        FSCursor cursor = (FSCursor) resolver.preserveQueryStateAndGet();
        if (cursor != null) {
            try {
                cursor.getCount();  // TODO: determine whether you need to make this call
            } catch (RuntimeException ex) {
                cursor.close();
                throw ex;
            }
        }
        return cursor;
    }

    @Override
    public void deliverResult(FSCursor cursor) {
        if (isReset()) {
            releaseResources(cursor);
            return;
        }

        FSCursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor) {
            releaseResources(oldCursor);
        }
    }

    @Override
    public void onCanceled(FSCursor cursor) {
        super.onCanceled(cursor);

        releaseResources(cursor);

        if (mObserver != null) {
            mObserver.unregister();
            mObserver = null;
        }
    }

    public G getApi() {
        return resolver.getApi();
    }

    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }

        if (mObserver == null) {
            mObserver = new MultiTableObserver();
        }

        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mCursor != null) {
            releaseResources(mCursor);
            mCursor = null;
        }

        if (mObserver != null) {
            mObserver.unregister();
            mObserver = null;
        }
    }

    private void releaseResources(FSCursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    private final class MultiTableObserver extends ContentObserver {

        public MultiTableObserver() {
            super(new Handler());

            // register for updates to all of the relevant tables
            if (tableUris != null) {
                for (Uri tableUri : tableUris) {
                    getContext().getContentResolver().registerContentObserver(tableUri, true, this);
                }
            }
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            onContentChanged();
        }

        public void unregister() {
            if (tableUris == null) {
                return;
            }
            getContext().getContentResolver().unregisterContentObserver(this);
        }
    }
}