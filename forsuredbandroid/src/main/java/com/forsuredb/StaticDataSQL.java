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

import android.content.Context;
import android.content.res.AssetManager;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSLogger;
import com.forsuredb.api.FSTableCreator;
import com.forsuredb.api.RecordContainer;
import com.forsuredb.api.staticdata.StaticDataRetrieverFactory;
import com.google.common.base.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*package*/ class StaticDataSQL {

    private final Class<? extends FSGetApi> apiClass;
    private final String tableName;
    private final String staticDataAsset;
    private final String staticDataRecordName;
    private final FSLogger log;

    /**
     * <p>
     *     A convenience for the constructor which takes the four parameters
     * </p>
     * @param tc
     */
    public StaticDataSQL(FSTableCreator tc) {
        this(tc.getTableApiClass(), tc.getTableName(), tc.getStaticDataAsset(), tc.getStaticDataRecordName());
    }

    public StaticDataSQL(Class<? extends FSGetApi> apiClass, String tableName, String staticDataAsset, String staticDataRecordName) {
        this.apiClass = apiClass;
        this.tableName = tableName;
        this.staticDataAsset = staticDataAsset;
        this.staticDataRecordName = staticDataRecordName;
        this.log = new ADBFSLogger(StaticDataSQL.class.getSimpleName());
    }

    public List<String> getInsertionSQL(Context context) {
        if (!canCreateStaticDataInsertionQueries()) {
            log.e("Cannot create static data insertion queries for apiClass: " + apiClass.getSimpleName());
            return Collections.EMPTY_LIST;
        }

        List<String> insertionQueries = new ArrayList<>();
        for (RecordContainer recordContainer : getRecordContainers(context.getResources().getAssets())) {
            insertionQueries.add(getInsertionQuery(recordContainer));
        }

        return insertionQueries;
    }

    private List<RecordContainer> getRecordContainers(AssetManager assetManager) {
        InputStream xmlStream = null;
        try {
            xmlStream = assetManager.open(staticDataAsset);
            return new StaticDataRetrieverFactory(log).fromStream(xmlStream).getRecords(staticDataRecordName);
        } catch (IOException ioe) {
            log.e("Could not retrieve static data from " + staticDataAsset + ":" + ioe.getMessage());
            return Collections.EMPTY_LIST;
        } finally {
            if (xmlStream != null) {
                try {
                    xmlStream.close();
                } catch (IOException e) {}
            }
        }
    }

    private String getInsertionQuery(RecordContainer recordContainer) {
        final StringBuilder queryBuf = new StringBuilder("INSERT INTO " + tableName + " (");
        final StringBuilder valueBuf = new StringBuilder();

        for (Method method : apiClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(FSColumn.class)) {
                continue;
            }
            final String columnName = getColumnName(method);
            if ("_id".equals(columnName)) {
                continue;   // <-- never insert an _id column
            }
            final Object val = recordContainer.get(getColumnName(method));
            if (val != null) {
                queryBuf.append(columnName).append(", ");
                valueBuf.append("'").append(val).append("', ");
            }
        }

        queryBuf.delete(queryBuf.length() - 2, queryBuf.length());  // <-- remove final ", "
        valueBuf.delete(valueBuf.length() - 2, valueBuf.length());  // <-- remove final ", "
        return queryBuf.append(") VALUES (").append(valueBuf.toString()).append(");").toString();
    }

    private String getColumnName(Method method) {
        return method.getAnnotation(FSColumn.class).value().isEmpty() ? method.getName() : method.getAnnotation(FSColumn.class).value();
    }

    private boolean canCreateStaticDataInsertionQueries() {
        return apiClass != null
                && !Strings.isNullOrEmpty(tableName)
                && !Strings.isNullOrEmpty(staticDataAsset)
                && !Strings.isNullOrEmpty(staticDataRecordName);
    }
}
