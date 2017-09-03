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
package com.fsryan.forsuredb;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.ForSureInfoFactory;
import com.fsryan.forsuredb.queryable.ContentProviderQueryable;
import com.fsryan.forsuredb.queryable.FSContentValues;
import com.fsryan.forsuredb.queryable.SQLiteDBQueryable;
import com.fsryan.forsuredb.queryable.FSJoinTranslator;

import java.util.List;

/**
 * <p>
 *     This is the main integration point for Android and ForSure. It tells ForSure the necessary
 *     information to resolve tables and query the underlying database. You should create an
 *     an instance of this class in your application's onCreate callback method and pass it to
 *     ForSure.init.
 * </p>
 * @author Ryan Scott
 */
public class ForSureAndroidInfoFactory implements ForSureInfoFactory<Uri, FSContentValues> {

    private static ForSureAndroidInfoFactory instance;

    private final Context appContext;
    private final String uriPrefix;
    private final boolean usingContentProvider;

    private ForSureAndroidInfoFactory(Context context, String authority) {
        appContext = context.getApplicationContext();
        uriPrefix = "content://" + authority;
        usingContentProvider = contentProviderResolved(appContext, authority);
    }

    /**
     * <p>
     *     Initializes the {@link ForSureAndroidInfoFactory}. Use this when you want
     *     your data to be accessible via a {@link android.content.ContentProvider}
     * </p>
     * @param appContext Your application's {@link Context}
     * @param authority the authority of your {@link android.content.ContentProvider}.
     */
    public static synchronized void init(Context appContext, @NonNull String authority) {
        if (instance == null) {
            instance = new ForSureAndroidInfoFactory(appContext, authority);
        }
    }

    /**
     * <p>
     *     Initializes the {@link ForSureAndroidInfoFactory}. Use this when you do not
     *     want your data to be accessible via a {@link android.content.ContentProvider}
     * </p>
     * @param appContext Your application's {@link Context}
     */
    public static synchronized void initWithoutContentProvider(Context appContext) {
        if (instance == null) {
            instance = new ForSureAndroidInfoFactory(appContext, SQLiteDBQueryable.AUTHORITY);
        }
    }

    public static ForSureAndroidInfoFactory inst() {
        if (instance == null) {
            throw new IllegalStateException("Must call init before inst");
        }
        return instance;
    }

    @Override
    public FSQueryable<Uri, FSContentValues> createQueryable(Uri resource) {
        return usingContentProvider ? new ContentProviderQueryable(appContext, resource) : new SQLiteDBQueryable(tableName(resource));
    }

    @Override
    public FSContentValues createRecordContainer() {
        return FSContentValues.getNew();
    }

    @Override
    public Uri tableResource(String tableName) {
        return Uri.parse(uriPrefix + "/" + tableName);
    }

    @Override
    public Uri locatorFor(String tableName, long id) {
        return Uri.withAppendedPath(tableResource(tableName), Long.toString(id));
    }

    @Override
    public Uri locatorWithJoins(Uri uri, List<FSJoin> joins) {
        return FSJoinTranslator.join(uri, tableName(uri), joins);
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

    // Kind of complicated because of the way this is done
    private static boolean contentProviderResolved(Context context, String authority) {
        final List<ProviderInfo> providers = context.getPackageManager().queryContentProviders(null, 0, 0);
        if (providers == null) {
            return false;
        }

        for (ProviderInfo providerInfo : providers) {
            final String providerPackage = providerInfo.applicationInfo.packageName;
            // In order to ensure that this is not a vector for attack, both the authority string and the
            // package name of the application that defined the provider are required to be checked.
            if (authority.equals(providerInfo.authority) && context.getPackageName().equals(providerPackage)) {
                return true;
            }
        }
        return false;
    }
}
