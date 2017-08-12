/*
   forsuredbandroid, an android companion to the forsuredb project

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
package com.fsryan.forsuredb.cursor;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.OperationCanceledException;

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.FSSaveApi;
import com.fsryan.forsuredb.api.Finder;
import com.fsryan.forsuredb.api.Resolver;
import com.fsryan.forsuredb.api.OrderBy;
import com.fsryan.forsuredb.provider.FSContentValues;

public class FSCursorLoader<T extends Resolver, G extends FSGetApi, S extends FSSaveApi<Uri>, F extends Finder<T, F>, O extends OrderBy<T, O>> extends AsyncTaskLoader<FSCursor> {

    private FSCursor mCursor;
    private Resolver<T, Uri, FSContentValues, G, S, F, O> resolver;
    private BaseResolverContentObserver<T> mObserver;
    private CancellationSignal mCancellationSignal;

    public FSCursorLoader(Context context, Resolver<T, Uri, FSContentValues, G, S, F, O> resolver) {
        super(context);
        this.resolver = resolver;
        mObserver = new BaseResolverContentObserver<T>(context, resolver, new Handler(), true) {
            @Override
            public void onChange(boolean selfChange, Uri tableUri, Resolver<T, Uri, FSContentValues, ?, ?, ?, ?> resolver) {
                onContentChanged();
            }
        };
    }

    @Override
    public FSCursor loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
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
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
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

        mObserver.unregister();
    }

    /**
     * <p>
     *     This is only the {@link G} extension of {@link FSGetApi}, so it will only give you
     *     methods to get the columns of the base table (the one originally queried) our of the
     *     {@link FSCursor}. In other words, you will have to get your own references to the
     *     {@link FSGetApi} extensions that are capable of getting fields out of the {@link FSCursor}.
     * </p>
     * @return an instance of the {@link G} extension of {@link FSGetApi} for the base table
     */
    public G getApi() {
        return resolver.getApi();
    }

    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }

        mObserver.register();

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

        mObserver.unregister();
    }

    private void releaseResources(FSCursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}