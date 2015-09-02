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
package com.forsuredb;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.FSTable;
import com.forsuredb.provider.DefaultProvider;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class FSTableCreator {

    private static final int NO_STATIC_DATA_RESOURCE_ID = -1;

    private final String authority;
    private final Class<? extends com.forsuredb.api.FSGetApi> tableApiClass;
    private final int staticDataResId;
    private final String staticDataRecordName;
    private final String tableName;

    public FSTableCreator(String authority, Class<? extends com.forsuredb.api.FSGetApi> tableApiClass, int staticDataResId, String staticDataRecordName) {
        this.authority = authority;
        this.tableApiClass = tableApiClass;
        this.staticDataResId = staticDataResId;
        this.staticDataRecordName = staticDataRecordName;
        this.tableName = tableApiClass.getAnnotation(FSTable.class).value();
    }

    public FSTableCreator(String authority, Class<? extends com.forsuredb.api.FSGetApi> tableApiClass) {
        this(authority, tableApiClass, NO_STATIC_DATA_RESOURCE_ID, "");
    }

    public FSTableCreator(Class<? extends com.forsuredb.api.FSGetApi> tableApiClass) {
        this(DefaultProvider.AUTHORITY, tableApiClass);
    }

    public String getTableName() {
        return tableName;
    }

    public String getAuthority() {
        return authority;
    }

    public Class<? extends com.forsuredb.api.FSGetApi> getTableApiClass() {
        return tableApiClass;
    }

    public int getStaticDataResId() {
        return staticDataResId;
    }

    public String getStaticDataRecordName() {
        return staticDataRecordName;
    }
    /**
     * <p>
     *     Returns a list of the insert statements that will be used to populate the static data in this table
     * </p>
     *
     * @return an empty list if there should be no static data
     */
    public List<String> getStaticInsertsSQL(Context context) {
        if (staticDataResId == NO_STATIC_DATA_RESOURCE_ID || Strings.isNullOrEmpty(staticDataRecordName)) {
            return Collections.EMPTY_LIST;
        }

        final String queryPrefix = "INSERT INTO " + tableName + " (";
        XmlResourceParser parser = context.getResources().getXml(staticDataResId);
        List<String> insertionQueries = Lists.newArrayList();
        try {
            parser.next();
            while (parser.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (startOfRecordTag(parser)) {
                    insertionQueries.add(getInsertionQuery(parser, queryPrefix));
                }
                parser.next();
            }
        } catch (XmlPullParserException e) {
            // do nothing
        } catch (IOException ioe) {
            // do nothing
        }

        return insertionQueries;
    }

    private String getInsertionQuery(XmlResourceParser parser, String queryPrefix) {
        final StringBuffer queryBuf = new StringBuffer(queryPrefix);
        final StringBuffer valueBuf = new StringBuffer();
        for (Method method : tableApiClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(FSColumn.class)) {
                continue;
            }
            final String columnName = getColumnName(method);
            if ("_id".equals(columnName)) {
                continue;   // <-- never insert an _id column
            }
            final String val = parser.getAttributeValue(null, columnName);
            if (!Strings.isNullOrEmpty(val)) {
                queryBuf.append(columnName).append(", ");
                valueBuf.append("'").append(val).append("', ");
            }
        }
        queryBuf.delete(queryBuf.length() - 2, queryBuf.length());  // <-- remove final ", "
        valueBuf.delete(valueBuf.length() - 2, valueBuf.length());  // <-- remove final ", "
        return queryBuf.append(") VALUES (").append(valueBuf.toString()).append(");").toString();
    }

    private boolean startOfRecordTag(XmlResourceParser parser) throws XmlPullParserException {
        return parser.getEventType() == XmlResourceParser.START_TAG
                && parser.getAttributeCount() > 0
                && staticDataRecordName.equals(parser.getName());
    }

    private String getColumnName(Method method) {
        return method.getAnnotation(FSColumn.class).value().isEmpty() ? method.getName() : method.getAnnotation(FSColumn.class).value();
    }
}
