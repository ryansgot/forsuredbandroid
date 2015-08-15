package com.forsuredb;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.forsuredb.migration.Migration;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class FSTableCreator {

    private static final String LOG_TAG = FSTableCreator.class.getSimpleName();

    private final String authority;
    private final Class<? extends FSGetApi> tableApiClass;
    private final int staticDataResId;
    private final String staticDataRecordName;

    public FSTableCreator(String authority, Class<? extends FSGetApi> tableApiClass, int staticDataResId, String staticDataRecordName) {
        this.authority = authority;
        this.tableApiClass = tableApiClass;
        this.staticDataResId = staticDataResId;
        this.staticDataRecordName = staticDataRecordName;
    }

    public FSTableCreator(String authority, Class<? extends FSGetApi> tableApiClass) {
        this(authority, tableApiClass, FSTableDescriber.NO_STATIC_DATA_RESOURCE_ID, "");
    }

    public String getAuthority() {
        return authority;
    }

    public Class<? extends FSGetApi> getTableApiClass() {
        return tableApiClass;
    }

    public int getStaticDataResId() {
        return staticDataResId;
    }

    public String getStaticDataRecordName() {
        return staticDataRecordName;
    }

    /**
     * <p>
     *     Gets a sorted list of migration sql relevant for this table
     * </p>
     * @param context
     * @return
     */
    public List<Migration> getMigrations(Context context) {
        AssetManager assetManager = context.getResources().getAssets();
        PriorityQueue<String> sortedPaths = createSortedMigrationFilenames(assetManager);

        List<Migration> retList = new LinkedList<>();
        while (sortedPaths.size() > 0) {
            retList.addAll(getMigrationsFromFile(assetManager, sortedPaths.remove()));
        }

        return retList;
    }

    private List<Migration> getMigrationsFromFile(AssetManager assetManager, String migrationFile) {
        InputStream in = null;
        XmlPullParser parser = null;
        try {
            parser = XmlPullParserFactory.newInstance().newPullParser();
            in = assetManager.open(migrationFile);
            parser.setInput(in, null);
            return getMigrationsFrom(parser);
        } catch (XmlPullParserException xppe) {
            Log.e(LOG_TAG, "getMigrationsFromFile: could not create new PullParser for migration " + migrationFile, xppe);
            return Collections.EMPTY_LIST;
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "getMigrationsFromFile: could not set input for migration " + migrationFile, ioe);
            return Collections.EMPTY_LIST;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe2) {
                    // can't do anything
                }
            }
        }
    }

    private List<Migration> getMigrationsFrom(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Migration> retList = new LinkedList<>();

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    Log.i(LOG_TAG, "getMigrationsFrom: document started: " + parser.getName());
                    break;
                case XmlPullParser.START_TAG:
                    if (!isMigrationTag(parser)) {
                        break;
                    }
                    Log.i(LOG_TAG, "getMigrationsFrom: tag started: " + parser.getName());
                    String tableName = parser.getAttributeValue(null, "table_name");
                    int dbVersion = 0;
                    try {
                        dbVersion = Integer.parseInt(parser.getAttributeValue(null, "db_version"));
                    } catch (NumberFormatException nfe) {

                    }
                    String query = parser.getAttributeValue(null, "query");
                    Log.i(LOG_TAG, "getMigrationsFrom: got query: " + query);
                    retList.add(Migration.builder().dbVersion(dbVersion).tableName(tableName).query(query).build());
                    break;
                case XmlPullParser.END_TAG:
                    Log.i(LOG_TAG, "getMigrationsFrom: tag ended: " + parser.getName());
                    break;
                case XmlPullParser.TEXT:
                    Log.i(LOG_TAG, "getMigrationsFrom: text event: " + parser.getText());
            }
            eventType = parser.next();
        }

        return retList;
    }

    private boolean isMigrationTag(XmlPullParser parser) {
        return parser != null && "migration".equals(parser.getName());
    }

    private PriorityQueue<String> createSortedMigrationFilenames(AssetManager assetManager) {
        PriorityQueue<String> retQueue = new PriorityQueue<>();
        try {
            for (String path : assetManager.list("")) {
                if (isMigrationXml(path)) {
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
