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

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSQueryable;
import com.fsryan.forsuredb.api.ForSureInfoFactory;
import com.fsryan.forsuredb.queryable.DirectLocator;
import com.fsryan.forsuredb.queryable.FSContentValues;
import com.fsryan.forsuredb.queryable.SQLiteDBQueryable;

import java.util.List;

/**
 * <p>
 *     This is the main integration point for Android and ForSure. It tells ForSure the necessary
 *     information to resolve tables and query the underlying database.
 * </p>
 */
public class ForSureAndroidInfoFactory implements ForSureInfoFactory<DirectLocator, FSContentValues> {

    private static ForSureAndroidInfoFactory instance = new ForSureAndroidInfoFactory();

    private ForSureAndroidInfoFactory() {}

    public static ForSureAndroidInfoFactory inst() {
        if (instance == null) {
            throw new IllegalStateException("Must call init before inst");
        }
        return instance;
    }

    @Override
    public FSQueryable<DirectLocator, FSContentValues> createQueryable(DirectLocator locator) {
        return new SQLiteDBQueryable(locator);
    }

    @Override
    public FSContentValues createRecordContainer() {
        return FSContentValues.getNew();
    }

    @Override
    public DirectLocator tableResource(String tableName) {
        return new DirectLocator(tableName);
    }

    @Override
    public DirectLocator locatorFor(String tableName, long id) {
        return new DirectLocator(tableName, id);
    }

    @Override
    public DirectLocator locatorWithJoins(DirectLocator locator, List<FSJoin> joins) {
        // TODO: locator with joins doesn't make sense because we're sending the joins in anyway in the call to the Queryable
        locator.addJoins(joins);
        return locator;
    }

    @Override
    public String tableName(DirectLocator locator) {
        return locator.table;
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
