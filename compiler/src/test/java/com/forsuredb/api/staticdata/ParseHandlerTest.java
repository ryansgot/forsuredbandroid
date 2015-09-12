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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class ParseHandlerTest {

    private SAXParser saxParser;
    private ByteArrayInputStream xmlStream;
    private final List<RecordContainer> parsedRecordContainers = new LinkedList<>();
    private final Parser.RecordListener recordListener = new Parser.RecordListener() {
        @Override
        public void onRecord(RecordContainer recordContainer) {
            parsedRecordContainers.add(recordContainer);
        }
    };

    private String inputXml;
    private String recordName;
    private List<Map<String, String>> expected;

    public ParseHandlerTest(String inputXml, String recordName, List<Map<String, String>> expected) {
        this.inputXml = inputXml;
        this.recordName = recordName;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // the basic success case with some values
                {
                        new StringBuffer().append("<static_data>\n")
                                          .append("<user global_id=\"1\" login_count=\"2\" app_rating=\"4.3\" competitor_app_rating=\"3.5\" />\n")
                                          .append("</static_data>")
                                          .toString(),
                        "user",
                        Lists.newArrayList(new ImmutableMap.Builder<String, String>().put("global_id", "1")
                                                                                     .put("login_count", "2")
                                                                                     .put("app_rating", "4.3")
                                                                                     .put("competitor_app_rating", "3.5")
                                                                                     .build())
                },
                // wrong record name (users instead of user)
                {
                        new StringBuffer().append("<static_data>\n")
                                          .append("<user global_id=\"1\" login_count=\"2\" app_rating=\"4.3\" competitor_app_rating=\"3.5\" />\n")
                                          .append("</static_data>")
                                          .toString(),
                        "users",
                        Lists.newArrayList()
                },
                // multiple records
                {
                        new StringBuffer().append("<static_data>\n")
                                          .append("<user global_id=\"1\" login_count=\"2\" app_rating=\"4.3\" competitor_app_rating=\"3.5\" />\n")
                                          .append("<user global_id=\"2\" login_count=\"3\" app_rating=\"5.4\" competitor_app_rating=\"4.6\" />\n")
                                          .append("</static_data>")
                                          .toString(),
                        "user",
                        Lists.newArrayList(new ImmutableMap.Builder<String, String>().put("global_id", "1")
                                                                                     .put("login_count", "2")
                                                                                     .put("app_rating", "4.3")
                                                                                     .put("competitor_app_rating", "3.5")
                                                                                     .build(),
                                           new ImmutableMap.Builder<String, String>().put("global_id", "2")
                                                                                     .put("login_count", "3")
                                                                                     .put("app_rating", "5.4")
                                                                                     .put("competitor_app_rating", "4.6")
                                                                                     .build())
                },
        });
    }

    @Before
    public void setUp() throws Exception {
        xmlStream = new ByteArrayInputStream(inputXml.getBytes());
        saxParser = SAXParserFactory.newInstance().newSAXParser();
    }

    @After
    public void tearDown() throws Exception {
        xmlStream.close();
        saxParser = null;
        parsedRecordContainers.clear();
    }

    @Test
    public void shouldHaveCorrectNumberOfRecords() throws Exception {
        saxParser.parse(xmlStream, new ParseHandler(recordName, recordListener, new FSLogger.DefaultFSLogger()));
        assertEquals(expected.size(), parsedRecordContainers.size());
    }

    @Test
    public void shouldHaveCorrectRecords() throws Exception {
        saxParser.parse(xmlStream, new ParseHandler(recordName, recordListener, new FSLogger.DefaultFSLogger()));
        while (0 < expected.size()) {
            validateMapEqualsRecordContainer(expected.remove(0), parsedRecordContainers.remove(0));
        }
    }

    private void validateMapEqualsRecordContainer(Map<String, String> expected, RecordContainer recordContainer) {
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            assertNotNull("Did not parse " + entry.getKey(), recordContainer.get(entry.getKey()));
            assertEquals(entry.getValue(), recordContainer.get(entry.getKey()));
        }
    }
}
