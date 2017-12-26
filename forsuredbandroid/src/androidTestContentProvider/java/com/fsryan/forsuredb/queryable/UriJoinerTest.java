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

import android.net.Uri;
import android.test.InstrumentationTestCase;

import com.fsryan.forsuredb.api.FSJoin;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UriJoinerTest extends InstrumentationTestCase {

    private static final String scheme = "content";
    private static final String authority = "com.fsryan.fosuredb.provider.UriJoinerTest.content";

    private Uri.Builder inputUriBuilder;
    private Uri.Builder expectedUriBuilder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        inputUriBuilder = new Uri.Builder().scheme(scheme)
                .authority(authority);
        expectedUriBuilder = new Uri.Builder().scheme(scheme)
                .authority(authority);
    }

    public void testAddOneParentJoinChildToTableContentUri() {
        final List<FSJoin> joins = Lists.newArrayList(createJoin(TableToJoin.PARENT, TableToJoin.CHILD1, FSJoin.Type.INNER));
        final Uri expected = expectedUriBuilder.appendPath(TableToJoin.PARENT.getTableName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD1.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName())
                .build();
        final Uri actual = UriJoinTranslator.join(inputUriBuilder.appendPath(TableToJoin.PARENT.getTableName()).build(), TableToJoin.PARENT.getTableName(), joins);
        assertEquals(expected, actual);
    }

    public void testAddOneChildJoinParentToTableContentUri() {
        final List<FSJoin> joins = Lists.newArrayList(createJoin(TableToJoin.PARENT, TableToJoin.CHILD1, FSJoin.Type.INNER));
        final Uri expected = expectedUriBuilder.appendPath(TableToJoin.CHILD1.getTableName())
                .appendQueryParameter("INNER JOIN", TableToJoin.PARENT.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName())
                .build();
        final Uri actual = UriJoinTranslator.join(inputUriBuilder.appendPath(TableToJoin.CHILD1.getTableName()).build(), TableToJoin.CHILD1.getTableName(), joins);
        assertEquals(expected, actual);
    }

    public void testAddMultipleSameTypeParentJoinChildToTableContentUri() {
        final List<FSJoin> joins = Lists.newArrayList(createJoin(TableToJoin.PARENT, TableToJoin.CHILD1, FSJoin.Type.INNER),
                createJoin(TableToJoin.PARENT, TableToJoin.CHILD2, FSJoin.Type.INNER));
        final Uri expected = expectedUriBuilder.appendPath(TableToJoin.PARENT.getTableName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD1.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD2.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD2.getTableName() + "." + TableToJoin.CHILD2.getColumnName())
                .build();
        final Uri actual = UriJoinTranslator.join(inputUriBuilder.appendPath(TableToJoin.PARENT.getTableName()).build(), TableToJoin.PARENT.getTableName(), joins);
        assertEquals(expected, actual);
    }

    public void testAddMultipleDifferentTypeParentJoinChildToTableContentUri() {
        final List<FSJoin> joins = Lists.newArrayList(createJoin(TableToJoin.PARENT, TableToJoin.CHILD1, FSJoin.Type.INNER),
                createJoin(TableToJoin.PARENT, TableToJoin.CHILD2, FSJoin.Type.OUTER));
        final Uri expected = expectedUriBuilder.appendPath(TableToJoin.PARENT.getTableName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD1.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName())
                .appendQueryParameter("OUTER JOIN", TableToJoin.CHILD2.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD2.getTableName() + "." + TableToJoin.CHILD2.getColumnName())
                .build();
        final Uri actual = UriJoinTranslator.join(inputUriBuilder.appendPath(TableToJoin.PARENT.getTableName()).build(), TableToJoin.PARENT.getTableName(), joins);
        assertEquals(expected, actual);
    }

    public void testAddGrandparentParentChildJoins() {
        final List<FSJoin> joins = Lists.newArrayList(createJoin(TableToJoin.PARENT, TableToJoin.CHILD1, FSJoin.Type.INNER),
                createJoin(TableToJoin.CHILD1, TableToJoin.CHILD1_CHILD, FSJoin.Type.INNER));
        final Uri expected = expectedUriBuilder.appendPath(TableToJoin.PARENT.getTableName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD1.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD1_CHILD.getTableName()
                        + " ON "
                        + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName()
                        + " = " + TableToJoin.CHILD1_CHILD.getTableName() + "." + TableToJoin.CHILD1_CHILD.getColumnName())
                .build();
        final Uri actual = UriJoinTranslator.join(inputUriBuilder.appendPath(TableToJoin.PARENT.getTableName()).build(), TableToJoin.PARENT.getTableName(), joins);
        assertEquals(expected, actual);
    }

    public void testGetSingleJoinStringFromUri() {
        final String expected = TableToJoin.PARENT.getTableName() + " INNER JOIN " + TableToJoin.CHILD1.getTableName()
                + " ON "
                + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName();
        final String actual = UriJoinTranslator.joinStringFrom(inputUriBuilder.appendPath(TableToJoin.PARENT.getTableName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD1.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName())
                .build());
        assertEquals(expected, actual);
    }

    public void testGetMultipleJoinStringFromUriDifferentJoinTypes() {
        final String expected = TableToJoin.PARENT.getTableName() + " INNER JOIN " + TableToJoin.CHILD1.getTableName()
                + " ON "
                + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName()
                + " OUTER JOIN " + TableToJoin.CHILD1_CHILD.getTableName()
                + " ON "
                + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName()
                + " = " + TableToJoin.CHILD1_CHILD.getTableName() + "." + TableToJoin.CHILD1_CHILD.getColumnName();
        final String actual = UriJoinTranslator.joinStringFrom(inputUriBuilder.appendPath(TableToJoin.PARENT.getTableName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD1.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName())
                .appendQueryParameter("OUTER JOIN", TableToJoin.CHILD1_CHILD.getTableName()
                        + " ON "
                        + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName()
                        + " = " + TableToJoin.CHILD1_CHILD.getTableName() + "." + TableToJoin.CHILD1_CHILD.getColumnName())
                .build());
        assertEquals(expected, actual);
    }

    public void testGetMultipleJoinStringFromUriSameJoinTypes() {
        final String expected = TableToJoin.PARENT.getTableName() + " INNER JOIN " + TableToJoin.CHILD1.getTableName()
                + " ON "
                + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName()
                + " INNER JOIN " + TableToJoin.CHILD1_CHILD.getTableName()
                + " ON "
                + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName()
                + " = " + TableToJoin.CHILD1_CHILD.getTableName() + "." + TableToJoin.CHILD1_CHILD.getColumnName();
        final String actual = UriJoinTranslator.joinStringFrom(inputUriBuilder.appendPath(TableToJoin.PARENT.getTableName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD1.getTableName()
                        + " ON "
                        + TableToJoin.PARENT.getTableName() + "." + TableToJoin.PARENT.getColumnName()
                        + " = " + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName())
                .appendQueryParameter("INNER JOIN", TableToJoin.CHILD1_CHILD.getTableName()
                        + " ON "
                        + TableToJoin.CHILD1.getTableName() + "." + TableToJoin.CHILD1.getColumnName()
                        + " = " + TableToJoin.CHILD1_CHILD.getTableName() + "." + TableToJoin.CHILD1_CHILD.getColumnName())
                .build());
        assertEquals(expected, actual);
    }

    private enum TableToJoin {
        PARENT("parent_table", "parent_table_column"),
        CHILD1("child1_table", "child1_table_column"),
        CHILD2("child2_table", "child2_table_column"),
        CHILD1_CHILD("child1_child_table", "child1_child_table_column");

        private String tableName;
        private String columnName;

        TableToJoin(String tableName, String columnName) {
            this.tableName = tableName;
            this.columnName = columnName;
        }

        public String getTableName() {
            return tableName;
        }

        public String getColumnName() {
            return columnName;
        }
    }

    private FSJoin createJoin(final TableToJoin parent, final TableToJoin child, final FSJoin.Type type) {
        Map<String, String> localToForeignColumnMap = new HashMap<>();
        localToForeignColumnMap.put(child.getColumnName(), parent.getColumnName());
        return new FSJoin(type, parent.getTableName(), child.getTableName(), localToForeignColumnMap);
    }
}
