/*
   forsuredbandroid, an android companion to the forsuredb project

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
package com.fsryan.forsuredb.queryable;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.fsryan.forsuredb.ForSureAndroidInfoFactory;
import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.util.MockUriBuilder;
import com.fsryan.forsuredb.util.UriUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public abstract class QueryCorrectorTest {

    private static final String[] emptySelectionArgs = new String[0];
    private static final String emptySelection = "";
    private static final String idIsSelection = "_id IS ?";
    private static final String idEqualsSelection = "_id = ?";

    @Mock
    protected Context mockContext;
    @Mock
    protected PackageManager mockPackageManager;

    protected QueryCorrector queryCorrectorUnderTest;

    // QueryHelper inputs
    protected Uri inputUri;
    protected String inputSelection;
    protected String[] inputSelectionArgs;

    public QueryCorrectorTest(Uri inputUri, String inputSelection, String[] inputSelectionArgs) {
        this.inputUri = inputUri;
        this.inputSelection = inputSelection;
        this.inputSelectionArgs = inputSelectionArgs;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getPackageManager()).thenReturn(mockPackageManager);

        ForSureAndroidInfoFactory.init(mockContext, "authority");

        queryCorrectorUnderTest = new QueryCorrector(inputUri, inputSelection, inputSelectionArgs);
    }

    @RunWith(Parameterized.class)
    public static class Selection extends QueryCorrectorTest {

        // expectations
        private String expectedSelection;
        private String[] expectedSelectionArgs;

        public Selection(Uri inputUri,
                         String inputSelection,
                         String[] inputSelectionArgs,
                         String expectedSelection,
                         String[] expectedSelectionArgs) {
            super(inputUri, inputSelection, inputSelectionArgs);
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

        @Test
        public void shouldHaveMatchingSelection() {
            assertEquals(expectedSelection, queryCorrectorUnderTest.getSelection(true));
        }

        @Test
        public void shouldHaveCorrectNumberOfSelectionArgs() {
            assertEquals(expectedSelectionArgs.length, queryCorrectorUnderTest.getSelectionArgs().length);
        }

        @Test
        public void shouldHaveMatchingSelectionArgs() {
            assertArrayEquals(expectedSelectionArgs, queryCorrectorUnderTest.getSelectionArgs());
        }
    }

    @RunWith(Parameterized.class)
    public static class FullFeaturedSelection extends QueryCorrectorTest {

        private final String expectedSelectionRetrieval;
        private final String expectedSelectionEdit;
        private final String[] expectedSelectionArgs;
        private final String expectedOrderBy;
        private final int expectedOffset;
        private final int expectedLimit;

        public FullFeaturedSelection(Uri inputUri,
                                     String expectedSelectionRetrieval,
                                     String expectedSelectionEdit,
                                     String[] expectedSelectionArgs,
                                     String expectedOrderBy,
                                     int expectedOffset,
                                     int expectedLimit) {
            super(inputUri, null, null);
            this.expectedSelectionRetrieval = expectedSelectionRetrieval;
            this.expectedSelectionEdit = expectedSelectionEdit;
            this.expectedSelectionArgs = expectedSelectionArgs;
            this.expectedOffset = expectedOffset;
            this.expectedLimit = expectedLimit;
            this.expectedOrderBy = expectedOrderBy;
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {   // 00: no limit or offset case
                            new MockUriBuilder("table")
                                    .orderBy(" ORDER BY table.column ASC")
                                    .build(),                  // inputUri
                            "",                                                 // expectedSelectionRetrieval
                            "",                                                 // expectedSelectionEdit
                            new String[0],                                      // expectedSelectionArgs
                            "table.column ASC",                                 // expectedOrderBy
                            0,                                                  // expectedOffset
                            0                                                   // expectedLimit
                    },
                    {   // 01: limit provided by first with no offset--should NOT flip ordering in inner select for update/delete query
                            new MockUriBuilder("table")
                                    .first(10)
                                    .orderBy(" ORDER BY table.column ASC")
                                    .build(),                                                  // inputUri
                            "",                                                                                 // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column ASC LIMIT 10)",    // expectedSelectionEdit
                            new String[0],                                                                      // expectedSelectionArgs
                            "table.column ASC",                                                                 // expectedOrderBy
                            0,                                                                                  // expectedOffset
                            10                                                                                  // expectedLimit
                    },
                    {   // 02: limit provided by last with no offset--should flip ordering in inner select for update/delete query
                            new MockUriBuilder("table")
                                    .last(10)
                                    .orderBy(" ORDER BY table.column ASC")
                                    .build(),                                                  // inputUri
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column DESC LIMIT 10)",   // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column DESC LIMIT 10)",   // expectedSelectionEdit
                            new String[0],                                                                      // expectedSelectionArgs
                            "table.column ASC",                                                                 // expectedOrderBy
                            0,                                                                                  // expectedOffset
                            10                                                                                  // expectedLimit
                    },
                    {   // 03: no order by clause and no offset should order by _id
                            new MockUriBuilder("table")
                                    .first(10)
                                    .build(),                                              // inputUri
                            "",                                                                             // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table._id ASC LIMIT 10)",   // expectedSelectionEdit
                            new String[0],                                                                  // expectedSelectionArgs
                            "",                                                                             // expectedOrderBy
                            0,                                                                              // expectedOffset
                            10                                                                              // expectedLimit
                    },
                    {   // 04: no order by clause should not be a problem when limiting from last
                            new MockUriBuilder("table")
                                    .last(10)
                                    .build(),                                              // inputUri
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table._id DESC LIMIT 10)",  // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table._id DESC LIMIT 10)",  // expectedSelectionEdit
                            new String[0],                                                                  // expectedSelectionArgs
                            "table._id ASC",                                                                // expectedOrderBy
                            0,                                                                              // expectedOffset
                            10                                                                              // expectedLimit
                    },
                    {   // 05: multiple order by clauses while limiting from first
                            new MockUriBuilder("table")
                                    .first(10)
                                    .orderBy(" ORDER BY table.column1 ASC, table.column2 DESC")
                                    .build(),                                                                      // inputUri
                            "",                                                                                                     // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column1 ASC, table.column2 DESC LIMIT 10)",   // expectedSelectionEdit
                            new String[0],                                                                                          // expectedSelectionArgs
                            "table.column1 ASC, table.column2 DESC",                                                                // expectedOrderBy
                            0,                                                                                                      // expectedOffset
                            10                                                                                                      // expectedLimit
                    },
                    {   // 06: multiple order by clauses while limiting from last
                            new MockUriBuilder("table")
                                    .last(10)
                                    .orderBy(" ORDER BY table.column1 ASC, table.column2 DESC")
                                    .build(),                                                                      // inputUri
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10)",   // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10)",   // expectedSelectionEdit
                            new String[0],                                                                                          // expectedSelectionArgs
                            "table.column1 ASC, table.column2 DESC",                                                                // expectedOrderBy
                            0,                                                                                                      // expectedOffset
                            10                                                                                                      // expectedLimit
                    },
                    {   // 07: offset clause by itself only affects update/delete queries
                            new MockUriBuilder("table")
                                    .orderBy(" ORDER BY table.column ASC")
                                    .offset(5)
                                    .build(),                                                  // inputUri
                            "",                                                                                 // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column ASC OFFSET 5)",    // expectedSelectionEdit
                            new String[0],                                                                      // expectedSelectionArgs
                            "table.column ASC",                                                                 // expectedOrderBy
                            5,                                                                                  // expectedOffset
                            0                                                                                   // expectedLimit
                    },
                    {   // 08: limit provided by first with offset--should NOT flip ordering in inner select for update/delete query
                            new MockUriBuilder("table")
                                    .first(10)
                                    .offset(5)
                                    .orderBy(" ORDER BY table.column ASC")
                                    .build(),                                                          // inputUri
                            "",                                                                                         // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column ASC LIMIT 10 OFFSET 5)",   // expectedSelectionEdit
                            new String[0],                                                                              // expectedSelectionArgs
                            "table.column ASC",                                                                         // expectedOrderBy
                            5,                                                                                          // expectedOffset
                            10                                                                                          // expectedLimit
                    },
                    {   // 09: limit provided by last with offset--should flip ordering in inner select for update/delete query
                            new MockUriBuilder("table")
                                    .last(10)
                                    .offset(5)
                                    .orderBy(" ORDER BY table.column ASC")
                                    .build(),                                                          // inputUri
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column DESC LIMIT 10 OFFSET 5)",  // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column DESC LIMIT 10 OFFSET 5)",  // expectedSelectionEdit
                            new String[0],                                                                              // expectedSelectionArgs
                            "table.column ASC",                                                                         // expectedOrderBy
                            5,                                                                                          // expectedOffset
                            10                                                                                          // expectedLimit
                    },
                    {   // 10: no order by clause with offset should order by _id
                            new MockUriBuilder("table")
                                    .first(10)
                                    .offset(5)
                                    .build(),                                                      // inputUri
                            "",                                                                                     // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table._id ASC LIMIT 10 OFFSET 5)",  // expectedSelectionEdit
                            new String[0],                                                                          // expectedSelectionArgs
                            "",                                                                                     // expectedOrderBy
                            5,                                                                                      // expectedOffset
                            10                                                                                      // expectedLimit
                    },
                    {   // 11: no order by clause with offset should order by _id DESC in inner SELECT query and ASC in outer retrieval
                            new MockUriBuilder("table")
                                    .last(10)
                                    .offset(5)
                                    .build(),                                                      // inputUri
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table._id DESC LIMIT 10 OFFSET 5)", // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table._id DESC LIMIT 10 OFFSET 5)", // expectedSelectionEdit
                            new String[0],                                                                          // expectedSelectionArgs
                            "table._id ASC",                                                                        // expectedOrderBy
                            5,                                                                                      // expectedOffset
                            10                                                                                      // expectedLimit
                    },
                    {   // 12: multiple order by clauses with offset while limiting from first
                            new MockUriBuilder("table")
                                    .first(10)
                                    .offset(5)
                                    .orderBy(" ORDER BY table.column1 ASC, table.column2 DESC")
                                    .build(),                                                                              // inputUri
                            "",                                                                                                             // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column1 ASC, table.column2 DESC LIMIT 10 OFFSET 5)",  // expectedSelectionEdit
                            new String[0],                                                                                                  // expectedSelectionArgs
                            "table.column1 ASC, table.column2 DESC",                                                                        // expectedOrderBy
                            5,                                                                                                              // expectedOffset
                            10                                                                                                              // expectedLimit
                    },
                    {   // 13: multiple order by clauses with offset while limiting from last
                            new MockUriBuilder("table")
                                    .last(10)
                                    .offset(5)
                                    .orderBy(" ORDER BY table.column1 ASC, table.column2 DESC")
                                    .build(),                                                                              // inputUri
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10 OFFSET 5)",  // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10 OFFSET 5)",  // expectedSelectionEdit
                            new String[0],                                                                                                  // expectedSelectionArgs
                            "table.column1 ASC, table.column2 DESC",                                                                        // expectedOrderBy
                            5,                                                                                                              // expectedOffset
                            10                                                                                                              // expectedLimit
                    },
                    {   // 14: single table join uri selecting last
                            new MockUriBuilder("table")
                                    .last(10)
                                    .addJoin(FSJoin.Type.INNER, "table", "child_table", "parent_column", "child_column")
                                    .orderBy(" ORDER BY table.column1 ASC, table.column2 DESC")
                                    .build(),                                                                              // inputUri
                            "table.rowid IN (SELECT table.rowid FROM table INNER JOIN child_table ON child_table.child_column = table.parent_column ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10)",  // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table INNER JOIN child_table ON child_table.child_column = table.parent_column ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10)",  // expectedSelectionEdit
                            new String[0],                                                                                                  // expectedSelectionArgs
                            "table.column1 ASC, table.column2 DESC",                                                                        // expectedOrderBy
                            0,                                                                                                              // expectedOffset
                            10                                                                                                              // expectedLimit
                    },
                    {   // 15: multiple table join uri selecting last
                            new MockUriBuilder("table")
                                    .last(10)
                                    .addJoin(FSJoin.Type.INNER, "table", "child_table", "parent_column", "child_column")
                                    .addJoin(FSJoin.Type.LEFT, "child_table", "grandchild_table", "child_column2", "grandchild_column")
                                    .orderBy(" ORDER BY table.column1 ASC, table.column2 DESC")
                                    .build(),                                                                              // inputUri
                            "table.rowid IN (SELECT table.rowid FROM table INNER JOIN child_table ON child_table.child_column = table.parent_column LEFT JOIN grandchild_table ON grandchild_table.grandchild_column = child_table.child_column2 ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10)",  // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table INNER JOIN child_table ON child_table.child_column = table.parent_column LEFT JOIN grandchild_table ON grandchild_table.grandchild_column = child_table.child_column2 ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10)",  // expectedSelectionEdit
                            new String[0],                                                                                                  // expectedSelectionArgs
                            "table.column1 ASC, table.column2 DESC",                                                                        // expectedOrderBy
                            0,                                                                                                              // expectedOffset
                            10                                                                                                              // expectedLimit
                    },
                    {   // 15: multiple table join uri with multiple join conditions selecting last
                            new MockUriBuilder("table")
                                    .last(10)
                                    .addJoin(FSJoin.Type.INNER, "table", "child_table", "parent_column", "child_column", "parent_column2", "child_column2")
                                    .addJoin(FSJoin.Type.LEFT, "child_table", "grandchild_table", "child_column2", "grandchild_column")
                                    .orderBy(" ORDER BY table.column1 ASC, table.column2 DESC")
                                    .build(),                                                                              // inputUri
                            "table.rowid IN (SELECT table.rowid FROM table INNER JOIN child_table ON child_table.child_column = table.parent_column AND child_table.child_column2 = table.parent_column2 LEFT JOIN grandchild_table ON grandchild_table.grandchild_column = child_table.child_column2 ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10)",  // expectedSelectionRetrieval
                            "table.rowid IN (SELECT table.rowid FROM table INNER JOIN child_table ON child_table.child_column = table.parent_column AND child_table.child_column2 = table.parent_column2 LEFT JOIN grandchild_table ON grandchild_table.grandchild_column = child_table.child_column2 ORDER BY table.column1 DESC, table.column2 ASC LIMIT 10)",  // expectedSelectionEdit
                            new String[0],                                                                                                  // expectedSelectionArgs
                            "table.column1 ASC, table.column2 DESC",                                                                        // expectedOrderBy
                            0,                                                                                                              // expectedOffset
                            10                                                                                                              // expectedLimit
                    }
                    // TODO: test join URIs with respect to the queries that have to get run.
                    // It is likely that joining when you have filtered with last() will end up crashing
            });
        }

        @Test
        public void shouldHaveExpectedSelectionWhenRetrieving() {
            assertEquals(expectedSelectionRetrieval, queryCorrectorUnderTest.getSelection(true));
        }

        @Test
        public void shouldOuputExpectedSelectionWhenUpdatingOrDeleting() {
            assertEquals(expectedSelectionEdit, queryCorrectorUnderTest.getSelection(false));
        }

        @Test
        public void shouldOuputExpectedSelectionArgs() {
            assertArrayEquals(expectedSelectionArgs, queryCorrectorUnderTest.getSelectionArgs());
        }

        @Test
        public void shouldOutputCorrectOrderBy() {
            assertEquals(expectedOrderBy, queryCorrectorUnderTest.getOrderBy());
        }

        @Test
        public void shouldOutputCorrectOffset() {
            assertEquals(expectedOffset, queryCorrectorUnderTest.getOffset());
        }

        @Test
        public void shouldOutputCorrectLimit() {
            assertEquals(expectedLimit, queryCorrectorUnderTest.getLimit());
        }
    }
}
