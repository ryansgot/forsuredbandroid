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

import android.net.Uri;
import android.util.Log;

import com.forsuredb.annotation.FSTable;
import com.forsuredb.api.FSFilter;
import com.forsuredb.api.FSFilterAdapter;
import com.forsuredb.api.FSGetAdapter;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSRecordResolver;
import com.forsuredb.api.FSSaveAdapter;
import com.forsuredb.api.FSSaveApi;
import com.forsuredb.api.FSSelection;
import com.forsuredb.api.FSTableCreator;
import com.forsuredb.api.Retriever;
import com.google.common.base.Strings;

/**
 * <p>
 *     The main description of a table. The main use of this class is getting objects
 *     of the {@link FSGetApi} extension and the {@link FSSaveApi} extension that are
 *     associated with this table.
 * </p>
 * @author Ryan Scott
 */
public class FSTableDescriber {

    private static final String LOG_TAG = FSTableDescriber.class.getSimpleName();

    private final String name;
    private final Class<? extends FSGetApi> getApiClass;
    private Class<? extends FSSaveApi<Uri>> saveApiClass;
    private Class<? extends FSFilter<Uri>> filterClass;
    private final String mimeType;
    private final Uri allRecordsUri;

    private FSGetApi getApi;

    /*package*/ FSTableDescriber(FSTableCreator fsTableCreator) throws IllegalStateException {
        validate(fsTableCreator);
        this.name = fsTableCreator.getTableName();
        this.getApiClass = fsTableCreator.getTableApiClass();
        mimeType = "vnd.android.cursor/" + name;
        allRecordsUri = Uri.parse("content://" + fsTableCreator.getAuthority() + "/" + name);
    }

    /*package*/ FSTableDescriber(String authority, Class<? extends FSGetApi> getApiClass) {
        this(new FSTableCreator(authority, getApiClass));
    }

    /**
     * @return the name if the table
     */
    public String getName() {
        return name;
    }

    /**
     * @return the mime type of this table as a String
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return the {@link Uri} representing all records of this table
     */
    public Uri getAllRecordsUri() {
        return allRecordsUri;
    }

    /**
     * @param id the id of the record for which you want a specific record {@link Uri}
     * @return the {@link Uri} representation of the specific record with the id
     */
    public Uri getSpecificRecordUri(long id) {
        return Uri.withAppendedPath(allRecordsUri, Long.toString(id));
    }

    public <F extends FSFilter<Uri>> F getNewFilter(FSRecordResolver<Uri, F> recordResolver) {
        return FSFilterAdapter.create((Class<F>) getFilterClass(), recordResolver);
    }

    /**
     * @return an object of the {@link FSGetApi} extension associated with this table.
     * You should cast this to the desired extension in order to use it.
     */
    public FSGetApi get() {
        if (getApi == null) {
            getApi = FSGetAdapter.create(getApiClass);
        }
        return getApi;
    }

    /**
     * @param cpq The {@link ContentProviderQueryable} capable of making queries for
     *            resources associated with this table
     * @return an object of the {@link FSSaveApi} extension associated with this table.
     * You should cast this to the desired extension in order to use it.
     */
    public FSSaveApi<Uri> set(ContentProviderQueryable cpq) {
        return FSSaveAdapter.create(cpq, FSContentValues.getNew(), getSaveApiClass());
    }

    /**
     * @return the {@link Class} object for the {@link FSGetApi} extension associated
     * with this table.
     */
    public Class<? extends FSGetApi> getGetApiClass() {
        return getApiClass;
    }

    /**
     * @return the {@link Class} object for the {@link FSSaveApi} extension associated
     * with this table.
     */
    public Class<? extends FSSaveApi<Uri>> getSaveApiClass() {
        if (saveApiClass == null) {
            initSaveApi();
        }
        return saveApiClass;
    }

    public Class<? extends FSFilter<Uri>> getFilterClass() {
        if (filterClass == null) {
            initFilterClass();
        }
        return filterClass;
    }

    private void initSaveApi() {
        final String className = getApiClass.getName() + "Setter";
        Class<?> loaded = null;
        try {
            loaded = Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            Log.e(LOG_TAG, "Could not find class: " + className, cnfe);
            throw new IllegalStateException("Cannot load the save api class because it was not found.");
        }
        try {
            saveApiClass = (Class<? extends com.forsuredb.api.FSSaveApi<Uri>>) loaded;
        } catch (ClassCastException cce) {
            Log.e(LOG_TAG, "Could not cast: " + loaded.getName() + " to correct class");
            throw cce;
        }
    }

    private void initFilterClass() {
        final String className = getApiClass.getName() + "Filter";
        Class<?> loaded = null;
        try {
            loaded = Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            Log.e(LOG_TAG, "Could not find class: " + className, cnfe);
            throw new IllegalStateException("Cannot load the filter api class because it was not found.");
        }
        try {
            filterClass = (Class<? extends FSFilter<Uri>>) loaded;
        } catch (ClassCastException cce) {
            Log.e(LOG_TAG, "Could not cast: " + loaded.getName() + " to correct class");
            throw cce;
        }
    }

    private void validate(FSTableCreator fsTableCreator) {
        if (fsTableCreator == null) {
            throw new IllegalArgumentException("Cannot create " + FSTableDescriber.class.getSimpleName() + " with null " + FSTableCreator.class.getSimpleName());
        }
        if (Strings.isNullOrEmpty(fsTableCreator.getAuthority())) {
            throw new IllegalArgumentException("Cannot create " + FSTableDescriber.class.getSimpleName() + " without an authority");
        }
        if (!fsTableCreator.getTableApiClass().isAnnotationPresent(FSTable.class)) {
            throw new IllegalArgumentException("Cannot create " + FSTableDescriber.class.getSimpleName() + " without a table name. Use the FSTable annotation on all " + com.forsuredb.api.FSGetApi.class.getSimpleName() + " extensions");
        }
    }
}
