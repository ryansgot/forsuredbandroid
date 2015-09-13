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
package com.forsuredb.api.staticdata;

import com.forsuredb.api.FSLogger;
import com.forsuredb.api.RecordContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * <p>
 *     Wrapper for a SAXParser that simplifies calls to parse
 * </p>
 * @author Ryan Scott
 */
/*package*/ class Parser {

    /*package*/ interface RecordListener {
        void onRecord(RecordContainer recordContainer);
    }

    private final RecordListener recordListener;
    private final FSLogger log;

    public Parser(FSLogger log, RecordListener recordListener) {
        this.log = log;
        this.recordListener = recordListener;
    }

    public final void parse(String staticDataFilePath, String recordName) throws FileNotFoundException {
        parse(new File(staticDataFilePath), recordName);
    }

    public final void parse(File staticDataFile, String recordName) throws FileNotFoundException {
        log.i("parsing: " + staticDataFile.getName());
        FileInputStream fis = new FileInputStream(staticDataFile);
        try {
            parse(fis, recordName);
        } finally {
            try {
                fis.close();
            } catch (IOException ioe) {
                // can't do anything about this
            }
        }
    }

    public final void parse(InputStream xmlStream, String recordName) {
        try {
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(xmlStream, new ParseHandler(recordName, recordListener, log));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
