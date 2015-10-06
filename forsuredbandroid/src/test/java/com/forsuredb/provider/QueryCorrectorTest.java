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
package com.forsuredb.provider;

import android.net.Uri;

import com.forsuredb.util.UriUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class QueryCorrectorTest {

    private static final String[] emptySelectionArgs = new String[0];
    private static final String emptySelection = "";
    private static final String idIsSelection = "_id IS ?";
    private static final String idEqualsSelection = "_id = ?";

    private QueryCorrector queryCorrectorUnderTest;

    // QueryHelper inputs
    private Uri inputUri;
    private String inputSelection;
    private String[] inputSelectionArgs;

    // expectations
    private String expectedSelection;
    private String[] expectedSelectionArgs;

    public QueryCorrectorTest(Uri inputUri,
                              String inputSelection,
                              String[] inputSelectionArgs,
                              String expectedSelection,
                              String[] expectedSelectionArgs) {
        this.inputUri = inputUri;
        this.inputSelection = inputSelection;
        this.inputSelectionArgs = inputSelectionArgs;
        this.expectedSelection = expectedSelection;
        this.expectedSelectionArgs = expectedSelectionArgs;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // an all records uri with empty selection and empty selection args
                {UriUtil.allRecordsUri(), emptySelection, emptySelectionArgs, emptySelection, emptySelectionArgs},
                // TODO: throw an exception when all records uri with id selection and empty selection args
//                {
//                        allRecordsUri(),
//                        "_id = ?",
//                        emptySelectionArgs,
//                        emptySelection,     // <-- we don't know what the user intended to select
//                        emptySelectionArgs
//                },
                // TODO: throw an exception when an all records uri with empty selection and a selection arg
//                {
//                        allRecordsUri(),
//                        emptySelection,
//                        new String[]{"1"},  // <-- we don't know what to do with this selection arg
//                        emptySelection,
//                        emptySelectionArgs
//                },
                // a specific record uri with = selection and correct selection args
                {UriUtil.specificRecordUri(1L), idEqualsSelection, new String[] {"1"}, idEqualsSelection, new String[] {"1"}},
                // a specific record uri with IS selection and correct selection args
                {UriUtil.specificRecordUri(1L), idIsSelection, new String[] {"1"}, idIsSelection, new String[] {"1"}},
                // a specific record uri with empty selection and empty selection args
                {UriUtil.specificRecordUri(1L), emptySelection, emptySelectionArgs, idEqualsSelection, new String[] {"1"}},
                // a specific record uri with empty selection and a correct _id selection arg
                {UriUtil.specificRecordUri(1L), emptySelection, new String[] {"1"}, idEqualsSelection, new String[] {"1"}},
                // a specific record uri with correct selection and no selection args
                {UriUtil.specificRecordUri(1L), idEqualsSelection, emptySelectionArgs, idEqualsSelection, new String[] {"1"}},
                // a specific record uri with an existing selection and that does not include the _id and selectionArgs that does not include the _id
                {UriUtil.specificRecordUri(1L), "something = ?", new String[] {"something"}, "_id = ? AND (something = ?)", new String[] {"1", "something"}},
                // a specific record uri with multiple existing selections and that does not include the _id and selectionArgs that does not include the _id
                {UriUtil.specificRecordUri(1L), "something = ? OR something_else = ?", new String[] {"something", "something_else"}, "_id = ? AND (something = ? OR something_else = ?)", new String[] {"1", "something", "something_else"}},
        });
    }

    @Before
    public void setUp() {
        queryCorrectorUnderTest = new QueryCorrector(inputUri, inputSelection, inputSelectionArgs);
    }

    @Test
    public void shouldHaveMatchingSelection() {
        assertEquals(expectedSelection, queryCorrectorUnderTest.getSelection());
    }

    @Test
    public void shouldHaveCorrectNumberOfSelectionArgs() {
        assertEquals(expectedSelectionArgs.length, queryCorrectorUnderTest.getSelectionArgs().length);
    }

    @Test
    public void shouldHaveMatchingSelectionArgs() {
        for (int i = 0; i < expectedSelectionArgs.length; i++) {
            assertEquals(expectedSelectionArgs[i], queryCorrectorUnderTest.getSelectionArgs()[i]);
        }
    }
}
