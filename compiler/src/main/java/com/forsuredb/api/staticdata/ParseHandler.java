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
import com.forsuredb.api.TypedRecordContainer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;

/**
 * <p>
 *     Handler for {@link SAXParser SAXParser} that is capable of handling static data XML
 * </p>
 * @author Ryan Scott
 */
/*package*/ class ParseHandler extends DefaultHandler {

    private final String recordName;
    private final Parser.RecordListener recordListener;
    private final FSLogger log;

    /*package*/ ParseHandler(String recordName, Parser.RecordListener recordListener, FSLogger log) {
        this.recordName = recordName;
        this.recordListener = recordListener;
        this.log = log == null ? new FSLogger.SilentLog() : log;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (!isRecordElement(qName)) {
            return;
        }

        log.i("found " + recordName);
        RecordContainer recordContainer = getRecordContainerFrom(attributes);
        if (recordContainer != null) {
            recordListener.onRecord(recordContainer);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        log.i("End Element :" + qName + " with localName: " + localName);
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        log.i("characters: " + new String(ch, start, length));
    }

    private boolean isRecordElement(String qName) {
        return qName != null && recordName.equals(qName);
    }

    private RecordContainer getRecordContainerFrom(Attributes attributes) {
        TypedRecordContainer ret = new TypedRecordContainer();
        for (int i = 0; i < attributes.getLength(); i++) {
            ret.put(attributes.getQName(i), attributes.getValue(i));
        }
        return ret;
    }
}
