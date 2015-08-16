package com.forsuredb.migration;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/*package*/ class Parser {

    public interface OnMigrationLineListener {
        void onMigrationLine(Migration migration);
    }

    private final OnMigrationLineListener listener;
    private final MigrationParseLogger log;

    public Parser(OnMigrationLineListener listener, MigrationParseLogger log) {
        this.listener = listener;
        this.log = log;
    }

    public final void parseMigrationFile(File migrationFile) {
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(migrationFile, new ParseHandler(listener, log));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public final void parseMigrationFile(String migrationFilePath) {
        parseMigrationFile(new File(migrationFilePath));
    }
}
