package com.fsryan.forsuredb.cursor;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.fsryan.forsuredb.api.Resolver;
import com.fsryan.forsuredb.queryable.FSContentValues;

/**
 * <p>
 *     Use this class to observe all tables referenced by the {@link Resolver} used to initialize
 *     the {@link ResolverContentObserver}
 * </p>
 * @param <R>
 */
public abstract class ResolverContentObserver<R extends Resolver> extends BaseResolverContentObserver<R> {

    public ResolverContentObserver(Context context,
                                   Resolver<R, Uri, FSContentValues, ?, ?, ?, ?> resolver,
                                   Handler handler,
                                   boolean deliverSelfNotifications) {
        super(context, resolver, handler, deliverSelfNotifications);
    }

    public final void onChange(boolean selfChange, Uri tableUri, Resolver<R, Uri, FSContentValues, ?, ?, ?, ?> resolver) {
        onChange(selfChange, tableUri, (FSCursor) resolver.preserveQueryStateAndGet());
    }

    /**
     * <p>
     *     This will get called any time a change is made to any table referenced by the
     *     {@link Resolver} used to initialize this {@link ResolverContentObserver}.
     * </p>
     * @param selfChange whether the observer made the change itself
     * @param tableUri the locator {@link Uri} of the table that was changed
     * @param cursor The {@link FSCursor} you can use to iterate through the result of using the
     *               {@link Resolver} to query your database.
     */
    public abstract void onChange(boolean selfChange, Uri tableUri, FSCursor cursor);
}
