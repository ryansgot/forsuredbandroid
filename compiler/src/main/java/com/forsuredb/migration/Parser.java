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
