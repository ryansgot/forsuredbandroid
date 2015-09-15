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

import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSSaveApi;
import com.forsuredb.api.FSTableCreator;
import com.google.common.collect.ImmutableBiMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *     Index for quick retrieval of {@link FSTableDescriber}, {@link FSSaveApi}, {@link FSGetApi},
 *     and {@link Uri} retreival as it pertains to tables managed by {@link ForSure}
 * </p>
 * @author Ryan Scott
 */
/*package*/ class FSIndex {

    private static final String LOG_TAG = FSIndex.class.getSimpleName();

    private final Map<Class<? extends FSGetApi>, FSTableDescriber> tableDescriberByGetApi = new HashMap<>();
    private final Map<Class<? extends FSSaveApi<Uri>>, FSTableDescriber> tableDescriberBySaveApi = new HashMap<>();
    private ImmutableBiMap<Uri, Class<? extends FSGetApi>> uriToGetApiMap;
    private ImmutableBiMap<Uri, Class<? extends FSSaveApi<Uri>>> uriToSaveApiMap;
    private final Map<String, FSTableDescriber> tableDescriberByName = new HashMap<>();

    /*package*/ FSIndex(List<FSTableCreator> tableCreators) {
        createTableDescriberMaps(tableCreators);
    }

    public boolean exists(String tableName) {
        return tableDescriberByName.containsKey(tableName);
    }

    public FSTableDescriber getByUri(Uri resource) {
        if (resource == null) {
            Log.e(LOG_TAG, "cannot resolve null resource");
            return null;
        }

        final List<String> segments = resource.getPathSegments();
        try {
            Long.parseLong(resource.getLastPathSegment());
            return getByName(segments.get(segments.size() - 2));
        } catch (NumberFormatException nfe) {}

        return getByName(resource.getLastPathSegment());
    }

    public FSTableDescriber getByName(String name) {
        return tableDescriberByName.get(name);
    }

    public <T extends FSSaveApi<Uri>> FSTableDescriber getBySaveApi(Class<T> saveApiClass) {
        if (saveApiClass == null) {
            return null;
        }
        return tableDescriberBySaveApi.get(saveApiClass);
    }

    public <T extends FSGetApi> FSTableDescriber getByGetApi(Class<T> getApiClass) {
        if (getApiClass == null) {
            return null;
        }
        return tableDescriberByGetApi.get(getApiClass);
    }

    public <T extends FSSaveApi<Uri>> Uri uriBySaveApiClass(Class<T> saveApiClass) {
        if (saveApiClass == null) {
            return null;
        }
        return uriToSaveApiMap.inverse().get(saveApiClass);
    }

    public <T extends FSGetApi> Uri uriByGetApiClass(Class<T> getApiClass) {
        if (getApiClass == null) {
            return null;
        }
        return uriToGetApiMap.inverse().get(getApiClass);
    }

    private void createTableDescriberMaps(List<FSTableCreator> tableCreators) {
        final ImmutableBiMap.Builder<Uri, Class<? extends FSGetApi>> uriToGetApiBuilder = ImmutableBiMap.builder();
        final ImmutableBiMap.Builder<Uri, Class<? extends FSSaveApi<Uri>>> uriToSaveApiBuilder = ImmutableBiMap.builder();

        for (FSTableCreator tableCreator : tableCreators) {
            addTable(new FSTableDescriber(tableCreator), uriToGetApiBuilder, uriToSaveApiBuilder);
        }

        uriToGetApiMap = uriToGetApiBuilder.build();
        uriToSaveApiMap = uriToSaveApiBuilder.build();
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
