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

import com.forsuredb.TestData;
import com.forsuredb.api.FSLogger;
import com.forsuredb.api.RecordContainer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;

/**
 * <p>
 *     Test whether the correct amount of migrations is parsed from one of the files in the test resources directory
 * </p>
 */
@RunWith(Parameterized.class)
public class ParserTest {


    private Parser.RecordListener rl;
    private Parser parser;

    private String xmlRecordFile;
    private String recordName;
    private int numRecords;

    public ParserTest(String xmlRecordFile, String recordName, int numRecords) {
        this.xmlRecordFile = xmlRecordFile;
        this.recordName = recordName;
        this.numRecords = numRecords;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        "profile_info.xml",
                        "profile_info",
                        10
                },
                {
                        "user.xml",
                        "user",
                        20
                }
        });
    }

    @Before
    public void setUp() {
        rl = Mockito.mock(Parser.RecordListener.class);
        parser = new Parser(new FSLogger.DefaultFSLogger(), rl);
    }

    @Test
    public void testParserFindsEachMigrationLine() throws Exception {
        parser.parse(TestData.TEST_RES + File.separator + xmlRecordFile, recordName);
        Mockito.verify(rl, Mockito.times(numRecords)).onRecord(Mockito.any(RecordContainer.class));
    }
}
