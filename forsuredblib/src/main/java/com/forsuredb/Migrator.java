package com.forsuredb;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.forsuredb.migration.Migration;
import com.forsuredb.migration.MigrationRetrieverFactory;
import com.google.common.base.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class Migrator {

    private static final String LOG_TAG = Migrator.class.getSimpleName();

    private final Context context;

    public Migrator(Context context) {
        this.context = context;
    }

    /**
     * @return a sorted list of Migration
     */
    public List<Migration> getMigrations() {
        final AssetManager assetManager = context.getResources().getAssets();
        final PriorityQueue<String> sortedPaths = createSortedMigrationFilenames(assetManager);

        FSLogger log = new ADBFSLogger();
        List<Migration> retList = new LinkedList<>();
        while (sortedPaths.size() > 0) {
            InputStream in = null;
            try {
                in = assetManager.open(sortedPaths.remove());
                retList.addAll(new MigrationRetrieverFactory(log).fromStream(in).getMigrations());
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ioe) {
                        // can't do anything about this
                    }
                }
            }
        }

        return retList;
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
        return filename != null && filename.endsWith("migration.xml");
    }

    private static class ADBFSLogger implements FSLogger {

        @Override
        public void e(String message) {
            Log.e(LOG_TAG, Strings.nullToEmpty(message));
        }

        @Override
        public void i(String message) {
            Log.i(LOG_TAG, Strings.nullToEmpty(message));
        }

        @Override
        public void w(String message) {
            Log.w(LOG_TAG, Strings.nullToEmpty(message));
        }

        @Override
        public void o(String message) {
            Log.d(LOG_TAG, Strings.nullToEmpty(message));
        }
    }
}