package com.forsuredb.migration;

import com.forsuredb.api.FSLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/*package*/ class Parser {

    public interface OnMigrationLineListener {
        void onMigrationLine(Migration migration);
    }

    private final OnMigrationLineListener listener;
    private final FSLogger log;

    public Parser(OnMigrationLineListener listener, FSLogger log) {
        this.listener = listener;
        this.log = log;
    }

    public final void parseMigration(File migrationFile) throws FileNotFoundException {
        log.i("parsing: " + migrationFile.getName());
        FileInputStream fis = new FileInputStream(migrationFile);
        try {
            parseMigration(fis);
        } finally {
            try {
                fis.close();
            } catch (IOException ioe) {
                // can't do anything about this
            }
        }
    }

    public final void parseMigration(String migrationFilePath) throws FileNotFoundException {
        parseMigration(new File(migrationFilePath));
    }

    public final void parseMigration(InputStream migrationInputStream) {
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(migrationInputStream, new ParseHandler(listener, log));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
