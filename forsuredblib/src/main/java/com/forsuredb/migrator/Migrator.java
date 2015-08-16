package com.forsuredb.migrator;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.forsuredb.migration.Migration;

import java.io.IOException;
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
        final MigrationParser parser = new MigrationParser(assetManager);

        List<Migration> retList = new LinkedList<>();
        while (sortedPaths.size() > 0) {
            retList.addAll(parser.parseMigrations(sortedPaths.remove()));
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
}