package com.forsuredb;

import android.net.Uri;
import android.util.Log;

import com.forsuredb.annotation.FSTable;
import com.forsuredb.api.FSGetAdapter;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSSaveAdapter;
import com.google.common.base.Strings;

public class FSTableDescriber {

    private static final String LOG_TAG = FSTableDescriber.class.getSimpleName();

    private final String name;
    private final Class<? extends com.forsuredb.api.FSGetApi> getApiClass;
    private Class<? extends com.forsuredb.api.FSSaveApi<Uri>> saveApiClass;
    private final String mimeType;
    private final Uri allRecordsUri;

    private com.forsuredb.api.FSGetApi getApi;
    private com.forsuredb.api.FSSaveApi<Uri> setApi;

    /*package*/ FSTableDescriber(FSTableCreator fsTableCreator) throws IllegalStateException {
        validate(fsTableCreator);
        this.name = fsTableCreator.getTableName();
        this.getApiClass = fsTableCreator.getTableApiClass();
        mimeType = "vnd.android.cursor/" + name;
        allRecordsUri = Uri.parse("content://" + fsTableCreator.getAuthority() + "/" + name);
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

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Uri getAllRecordsUri() {
        return allRecordsUri;
    }

    public Uri getSpecificRecordUri(long id) {
        return Uri.withAppendedPath(allRecordsUri, Long.toString(id));
    }

    public FSGetApi get() {
        if (getApi == null) {
            getApi = FSGetAdapter.create(getApiClass);
        }
        return getApi;
    }

    public com.forsuredb.api.FSSaveApi set(ContentProviderQueryable q) {
        if (setApi == null) {
            setApi = FSSaveAdapter.create(q, FSContentValues.getNew(), getSaveApiClass());
        }
        return setApi;
    }

    public Class<? extends com.forsuredb.api.FSGetApi> getGetApiClass() {
        return getApiClass;
    }

    public Class<? extends com.forsuredb.api.FSSaveApi<Uri>> getSaveApiClass() {
        if (saveApiClass == null) {
            initSaveApi();
        }
        return saveApiClass;
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
}
