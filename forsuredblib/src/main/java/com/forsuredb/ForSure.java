package com.forsuredb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.google.common.collect.ImmutableBiMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForSure {

    private final Context appContext;
    private final Map<Class<? extends FSGetApi>, FSTableDescriber> tableDescriberByGetApi = new HashMap<>();
    private final Map<Class<? extends FSSaveApi<Uri>>, FSTableDescriber> tableDescriberBySaveApi = new HashMap<>();
    private ImmutableBiMap<Uri, Class<? extends FSGetApi>> uriToGetApiMap;
    private ImmutableBiMap<Uri, Class<? extends FSSaveApi<Uri>>> uriToSaveApiMap;
    private final Map<String, FSTableDescriber> tableDescriberByName = new HashMap<>();

    private ForSure(Context appContext, List<FSTableCreator> tableCreators) {
        this.appContext = appContext;
        createTableDescriberMaps(tableCreators);
    }

    private static class Holder {
        public static ForSure instance;
    }

    /**
     * <p>
     *     Initializes the underlying database tables and
     * </p>
     * @param context
     * @param tableCreators
     */
    public static void init(Context context, String dbName, List<FSTableCreator> tableCreators) {
        if (Holder.instance == null) {
            Holder.instance = new ForSure(context.getApplicationContext(), tableCreators);
            FSDBHelper.init(context.getApplicationContext(), dbName, tableCreators);
        }
    }

    public static ForSure inst() throws IllegalStateException {
        if (Holder.instance == null) {
            throw new IllegalStateException("Must call ForSure.init method prior to getting an instance");
        }
        return Holder.instance;
    }

    private void createTableDescriberMaps(List<FSTableCreator> tableCreators) {
        final ImmutableBiMap.Builder<Uri, Class<? extends FSGetApi>> uriToGetApiBuilder = ImmutableBiMap.builder();
        final ImmutableBiMap.Builder<Uri, Class<? extends FSSaveApi<Uri>>> uriToSaveApiBuilder = ImmutableBiMap.builder();

        for (FSTableCreator tableCreator : tableCreators) {
            final FSTableDescriber table = new FSTableDescriber(tableCreator);
            addTable(table, uriToGetApiBuilder, uriToSaveApiBuilder);
        }

        uriToGetApiMap = uriToGetApiBuilder.build();
        uriToSaveApiMap = uriToSaveApiBuilder.build();
    }

    public SQLiteDatabase getReadableDatabase() {
        return FSDBHelper.inst().getReadableDatabase();
    }

    public SQLiteDatabase getWritableDatabase() {
        return FSDBHelper.inst().getWritableDatabase();
    }

    /**
     * <p>
     *     Do not pass in a specific-record Uri. Only pass in an all-records Uri for now
     * </p>
     * @param resource
     * @param <T>
     * @return
     */
    public <T extends FSGetApi> T getApi(Uri resource) {
        return resource == null ? null : (T) tableDescriberByGetApi.get(uriToGetApiMap.get(resource)).get();
    }

    /**
     * <p>
     *     Do not pass in a specific-record Uri. Only pass in an all-records Uri for now
     * </p>
     * @param resource
     * @param <T>
     * @return
     */
    public <T extends FSSaveApi<Uri>> T setApi(Uri resource) {
        if (resource == null) {
            return null;
        }
        final ContentProviderQueryable cpq = new ContentProviderQueryable(appContext, resource);
        final Class<? extends FSSaveApi<Uri>> saveApi = uriToSaveApiMap.get(resource);
        return (T) tableDescriberBySaveApi.get(saveApi).set(cpq);
    }

    public <T extends FSGetApi> T getApi(Class<T> getApiClass) {
        return getApiClass == null ? null : (T) tableDescriberByGetApi.get(getApiClass).get();
    }

    public <T extends FSSaveApi<Uri>> T setApi(Class<T> saveApiClass) {
        if (saveApiClass == null) {
            return null;
        }
        final Uri resource = uriToSaveApiMap.inverse().get(saveApiClass);
        final ContentProviderQueryable cpq = new ContentProviderQueryable(appContext, resource);
        return (T) tableDescriberBySaveApi.get(saveApiClass).set(cpq);
    }

    public FSTableDescriber getTable(String tableName) {
        return tableName == null ? null : tableDescriberByName.get(tableName);
    }

    public boolean containsTable(String tableName) {
        return tableDescriberByName.containsKey(tableName);
    }

    private void addTable(FSTableDescriber fsTableDescriber,
                          ImmutableBiMap.Builder<Uri, Class<? extends FSGetApi>> uriToGetApiBuilder,
                          ImmutableBiMap.Builder<Uri, Class<? extends FSSaveApi<Uri>>> uriToSaveApiBuilder) {
        tableDescriberByGetApi.put(fsTableDescriber.getGetApiClass(), fsTableDescriber);
        uriToGetApiBuilder.put(fsTableDescriber.getAllRecordsUri(), fsTableDescriber.getGetApiClass());
        tableDescriberBySaveApi.put(fsTableDescriber.getSaveApiClass(), fsTableDescriber);
        uriToSaveApiBuilder.put(fsTableDescriber.getAllRecordsUri(), fsTableDescriber.getSaveApiClass());
        tableDescriberByName.put(fsTableDescriber.getName(), fsTableDescriber);
    }
}
