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
import android.content.res.AssetManager;
import android.util.Log;

import com.fsryan.forsuredb.api.migration.MigrationRetrieverFactory;
import com.fsryan.forsuredb.migration.MigrationSet;
import com.fsryan.forsuredb.serialization.FSDbInfoSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/*package*/ class Migrator {

    private static final String LOG_TAG = Migrator.class.getSimpleName();

    private final Context context;
    private final FSDbInfoSerializer serializer;
    private List<MigrationSet> migrationSets;

    /*package*/ Migrator(Context context, FSDbInfoSerializer serializer) {
        this.context = context;
        this.serializer = serializer;
    }

    /**
     * @return a sorted list of Migration
     */
    public List<MigrationSet> getMigrationSets() {
        if (migrationSets == null) {
            createMigrationSets();
        }
        return migrationSets;
    }

    private void createMigrationSets() {
        final AssetManager assetManager = context.getResources().getAssets();
        final PriorityQueue<String> sortedPaths = createSortedMigrationFilenames(assetManager);

        migrationSets = new LinkedList<>();
        while (sortedPaths.size() > 0) {
            addMigrationsFromFile(assetManager, sortedPaths.remove());
        }
    }

    private void addMigrationsFromFile(AssetManager assetManager, String filename) {
        InputStream in = null;
        try {
            in = assetManager.open(filename);
            // TODO: this has got to be a plugin model to avoid the direct dependency on Gson
            migrationSets.addAll(new MigrationRetrieverFactory(serializer, new ADBFSLogger(LOG_TAG)).fromStream(in).getMigrationSets());
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {}
            }
        }
    }

    private PriorityQueue<String> createSortedMigrationFilenames(AssetManager assetManager) {
        PriorityQueue<String> retQueue = new PriorityQueue<>();
        try {
            for (String path : assetManager.list("")) {
                if (isMigrationXml(path)) {
                    Log.i(LOG_TAG, "createSortedMigrationFilenames: adding file to queue: " + path);
                    retQueue.add(path);
                }
            }
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "createSortedMigrationFilenames: IOException when iterating through assets in directory \"\"", ioe);
        }

        return retQueue;
    }

    private boolean isMigrationXml(String filename) {
        return filename != null && filename.endsWith("migration.json");
    }
}
