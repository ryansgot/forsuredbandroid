package com.forsuredb.migrator;

import android.content.res.AssetManager;
import android.util.Log;

import com.forsuredb.migration.Migration;
import com.google.common.base.Strings;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/*package*/ class MigrationParser {

    private static final String LOG_TAG = MigrationParser.class.getSimpleName();

    private final AssetManager assetManager;

    /*package*/ MigrationParser(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public List<Migration> parseMigrations(String migrationFile) {
        Log.i(LOG_TAG, "parsing migration file: " + migrationFile);
        InputStream in = null;
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            in = assetManager.open(migrationFile);
            parser.setInput(in, null);
            return getMigrationsFrom(parser);
        } catch (XmlPullParserException xppe) {
            Log.e(LOG_TAG, "parseMigrations: could not create new PullParser for migration " + migrationFile, xppe);
            return Collections.EMPTY_LIST;
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "parseMigrations: could not set input for migration " + migrationFile, ioe);
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
            if (isParsingMigration(eventType, parser.getName())) {
                Migration migration = pullFrom(parser);
                if (migration != null) {
                    retList.add(migration);
                }
            }

            eventType = parser.next();
        }

        return retList;
    }

    private Migration pullFrom(XmlPullParser parser) {
        int dbVersion;
        try {
            dbVersion = Integer.parseInt(parser.getAttributeValue(null, "db_version"));
        } catch (NumberFormatException nfe) {
            Log.e(LOG_TAG, "cannot create a migration without a db_version");
            return null;
        }

        String tableName = parser.getAttributeValue(null, "table_name");
        if (Strings.isNullOrEmpty(tableName)) {
            Log.e(LOG_TAG, "cannot create a migration without a table_name");
            return null;
        }

        String query = parser.getAttributeValue(null, "query");
        if (Strings.isNullOrEmpty(query)) {
            Log.e(LOG_TAG, "cannot create a migration without a query");
            return null;
        }

        return Migration.builder().dbVersion(dbVersion).tableName(tableName).query(query).build();
    }

    private boolean isParsingMigration(int eventType, String tag) {
        return eventType == XmlPullParser.START_TAG && "migration".equals(tag);
    }
}
