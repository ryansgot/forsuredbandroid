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
package com.fsryan.fosuredb;

import android.content.Context;
import android.content.res.AssetManager;

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.FSLogger;
import com.fsryan.forsuredb.api.FSTableCreator;
import com.fsryan.forsuredb.api.staticdata.StaticDataRetrieverFactory;
import com.fsryan.forsuredb.sqlitelib.SqlGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

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
            return Collections.emptyList();
        }

        SqlGenerator sqlGenerator = new SqlGenerator();
        List<String> insertionQueries = new ArrayList<>();
        for (Map<String, String> rawRecord : getRawRecords(context.getResources().getAssets())) {
            insertionQueries.add(sqlGenerator.newSingleRowInsertionSql(tableName, rawRecord));
        }

        return insertionQueries;
    }

    private List<Map<String, String>> getRawRecords(AssetManager assetManager) {
        InputStream xmlStream = null;
        try {
            xmlStream = assetManager.open(staticDataAsset);
            return new StaticDataRetrieverFactory(log).fromStream(xmlStream).getRawRecords(staticDataRecordName);
        } catch (IOException ioe) {
            log.e("Could not retrieve static data from " + staticDataAsset + ":" + ioe.getMessage());
            return Collections.emptyList();
        } finally {
            if (xmlStream != null) {
                try {
                    xmlStream.close();
                } catch (IOException e) {}
            }
        }
    }

    private boolean canCreateStaticDataInsertionQueries() {
        return !isNullOrEmpty(tableName) && !isNullOrEmpty(staticDataAsset) && !isNullOrEmpty(staticDataRecordName);
    }
}
