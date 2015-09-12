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

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class StaticDataRetrieverFactory {

    private final FSLogger log;

    public StaticDataRetrieverFactory(FSLogger log) {
        this.log = log == null ? new FSLogger.SilentLog() : log;
    }

    public StaticDataRetriever fromStream(final InputStream xmlStream) {
        if (xmlStream == null) {
            return new EmptyRetriever();
        }

        return new StaticDataRetriever() {
            List<RecordContainer> records;

            @Override
            public List<RecordContainer> getRecords(final String recordName) {
                if (records != null) {
                    return records;
                }

                records = new LinkedList<>();
                new Parser(log, new Parser.RecordListener() {
                    @Override
                    public void onRecord(RecordContainer recordContainer) {
                        records.add(recordContainer);
                    }
                }).parse(xmlStream, recordName);

                return records;
            }
        };
    }

    private static class EmptyRetriever implements StaticDataRetriever {
        @Override
        public List<RecordContainer> getRecords(String recordName) {
            return Collections.EMPTY_LIST;
        }
    }
}
