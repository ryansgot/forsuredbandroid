package com.fsryan.forsuredb.cursor;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.fsryan.forsuredb.api.Resolver;
import com.fsryan.forsuredb.queryable.FSContentValues;
import com.fsryan.forsuredb.queryable.UriEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     A base class for observing all tables referenced by a resolver. In most cases, you will
 *     not want to use this directly. You'll probably want to use {@link ResolverContentObserver}
 *     instead of this class so that you don't accidentally destroy the query you've built up in
 *     the {@link Resolver} passed into this observer
 * </p>
 * @param <R>
 */
/*package*/ abstract class BaseResolverContentObserver<R extends Resolver> {

    private final Context context;
    private final List<Uri> tableUris;
    private final Map<Uri, ContentObserver> contentObservers = new HashMap<>();

    public BaseResolverContentObserver(Context context,
                                       final Resolver<R, Uri, FSContentValues, ?, ?, ?, ?> resolver,
                                       Handler handler,
                                       final boolean deliverSelfNotifications) {
        this.context = context;
        this.tableUris = UriEvaluator.tableReferences(resolver.currentLocator());
        for (final Uri tableUri : tableUris) {
            contentObservers.put(tableUri, new ContentObserver(handler) {

                @Override
                public boolean deliverSelfNotifications() {
                    return deliverSelfNotifications;
                }

                @Override
                public void onChange(boolean selfChange) {
                    BaseResolverContentObserver.this.onChange(selfChange, tableUri, resolver);
                }
            });
        }
    }

    /**
     * <p>
     *     Register all {@link ContentObserver} instances associated with this
     *     {@link BaseResolverContentObserver}. Note that if there are any lifecycle considerations, you
     *     must take care to unregister this content observer
     * </p>
     * @see #unregister()
     */
    public final void register() {
        for (Uri tableUri : tableUris) {
            context.getContentResolver().registerContentObserver(tableUri, true, contentObservers.get(tableUri));
        }
    }

    /**
     * <p>
     *     Unregisters all observers associated with this {@link BaseResolverContentObserver}
     * </p>
     */
    public final void unregister() {
        for (Uri tableUri : tableUris) {
            context.getContentResolver().unregisterContentObserver(contentObservers.get(tableUri));
        }
    }

    /**
     * <p>
     *     You must override this method to react to any change in any of the tables referenced
     *     by the {@link Resolver} used to initialize this {@link BaseResolverContentObserver}
     * </p>
     * @param selfChange if this is a self-change notification
     * @param tableUri the Uri of the table that was changed
     * @param resolver call {@link Resolver#preserveQueryStateAndGet()} to ensure that you do not
     *                 bash over the built up query.
     */
    public abstract void onChange(boolean selfChange, Uri tableUri, Resolver<R, Uri, FSContentValues, ?, ?, ?, ?> resolver);
}
