package com.forsuredb;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.net.Uri;

import com.forsuredb.record.FSColumn;
import com.forsuredb.record.FSApi;
import com.forsuredb.record.ForeignKey;
import com.forsuredb.record.PrimaryKey;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class FSTableDescriber {

    /*package*/ static final int NO_STATIC_DATA_RESOURCE_ID = -1;

    private final String name;
    private final Class<? extends FSApi> tableApiClass;
    private final int staticDataResId;
    private final String staticDataRecordName;
    private final String mimeType;
    private final Uri allRecordsUri;

    private String tableCreateQuery;
    private FSApi tableApi;

    /*package*/ FSTableDescriber(FSTableCreator FSTableCreator) throws IllegalStateException {
        this(FSTableCreator.getAuthority(), FSTableCreator.getTableApiClass(), FSTableCreator.getStaticDataResId(), FSTableCreator.getStaticDataRecordName());
    }

    private FSTableDescriber(String authority, Class<? extends FSApi> tableApiClass, int staticDataResId, String staticDataRecordName)
                                                                                                    throws IllegalStateException {
        validate(authority, tableApiClass);
        this.name = tableApiClass.getAnnotation(FSTable.class).value();
        this.tableApiClass = tableApiClass;
        this.staticDataResId = staticDataResId;
        this.staticDataRecordName = staticDataRecordName;
        mimeType = "vnd.android.cursor/" + name;
        allRecordsUri = Uri.parse("content://" + authority + "/" + name);
    }

    public String getName() {
        return name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Uri getAllRecordsUri() {
        return allRecordsUri;
    }

    public Uri getSpecificRecordUri(long id) {
        return Uri.withAppendedPath(allRecordsUri, Long.toString(id));
    }

    public String getTableCreateQuery() {
        if (tableCreateQuery == null) {
            tableCreateQuery = buildTableCreateQuery();
        }
        return tableCreateQuery;
    }

    public FSApi getTableApi() {
        if (tableApi == null) {
            tableApi = FSAdapter.create(tableApiClass);
        }
        return tableApi;
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

        final String queryPrefix = "INSERT INTO " + name + " (";
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

    // Private methods to help with making the table create query

    private String buildTableCreateQuery() {
        StringBuffer queryBuffer = new StringBuffer("CREATE TABLE ").append(name).append("(");

        for (Method method : tableApiClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(FSColumn.class)) {
                continue;
            }
            appendColumnDefinitionTo(queryBuffer, method);
            queryBuffer.append(", ");
        }
        queryBuffer.delete(queryBuffer.length() - 2, queryBuffer.length()); // <-- remove final ", "
        appendForeignKeysLineTo(queryBuffer);
        return queryBuffer.append(");").toString();
    }

    private void appendColumnDefinitionTo(StringBuffer queryBuffer, Method method) {
        final TypeTranslator typeTranslator = TypeTranslator.getFrom(method.getGenericReturnType());
        queryBuffer.append(getColumnName(method)).append(" ")
                   .append(typeTranslator == null ? "" : typeTranslator.getSQLiteTypeString())
                   .append(method.isAnnotationPresent(PrimaryKey.class) ? " " + method.getAnnotation(PrimaryKey.class).definitionText() : "");
    }

    private boolean isForeignKey(Method method) {
        return method.isAnnotationPresent(ForeignKey.class);
    }

    private void appendForeignKeysLineTo(StringBuffer queryBuffer) {
        for (Method method : tableApiClass.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(FSColumn.class) || !isForeignKey(method)) {
                continue;
            }
            final String columnName = getColumnName(method);
            final ForeignKey foreignKey = method.getAnnotation(ForeignKey.class);
            queryBuffer.append(", FOREIGN KEY(")
                       .append(columnName)
                       .append(") REFERENCES ")
                       .append(foreignKey.apiClass().getAnnotation(FSTable.class).value())
                       .append("(").append(foreignKey.columnName())
                       .append(")");
        }
    }

    private String getColumnName(Method method) {
        return method.getAnnotation(FSColumn.class).value().isEmpty() ? method.getName() : method.getAnnotation(FSColumn.class).value();
    }


    /**
     * <p>
     *     Validates the properties of this table to be correct
     * </p>
     */
    private void validate(String authority, Class<? extends FSApi> tableApi) throws IllegalStateException, IllegalArgumentException {
        if (!tableApi.isAnnotationPresent(FSTable.class)) {
            throw new IllegalArgumentException("Cannot create table without a table name. Use the FSTable annotation on all FSDataModel extensions");
        }
        final String name = tableApi.getAnnotation(FSTable.class).value();
        if (ForSure.getInstance().containsTable(name)) {
            throw new IllegalArgumentException("Cannot create table named " + name + "; that table already exists.");
        }
        if (authority == null) {
            throw new IllegalArgumentException ("Cannot create table with null authority");
        }
        for (Method method : tableApi.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(FSColumn.class) || !isForeignKey(method)) {
                continue;
            }
            final ForeignKey foreignKey = method.getAnnotation(ForeignKey.class);
            validateForeignKeyRelationship(method, foreignKey);
        }
    }

    private void validateForeignKeyRelationship(Method method, ForeignKey foreignKey) throws IllegalStateException, IllegalArgumentException {
        validateForeignKeyTable(foreignKey.apiClass());
        validateForeignKeyPresence(method, foreignKey);
    }

    private void validateForeignKeyTable(Class<? extends FSApi> foreignTableApiClass) {
        if (!foreignTableApiClass.isAnnotationPresent(FSTable.class)) {
            throw new IllegalArgumentException("ForeignKey apiClass must be a class annotated with the FSTable annotation");
        }
        final String foreignTableName = foreignTableApiClass.getAnnotation(FSTable.class).value();
        final ForSure forSure = ForSure.getInstance();
        if (!forSure.containsTable(foreignTableName)) {
            throw new IllegalStateException("Must create table " + foreignTableName + " prior to creating table " + name);
        }
    }

    private void validateForeignKeyPresence(Method method, ForeignKey foreignKey) {
        final Class<? extends FSApi> foreignTableApiClass = foreignKey.apiClass();
        final String foreignTableName = foreignTableApiClass.getAnnotation(FSTable.class).value();
        boolean foreignKeyExists = false;
        Type foreignKeyType = null;
        for (Method foreignMethod : foreignTableApiClass.getDeclaredMethods()) {
            if (!foreignMethod.isAnnotationPresent(FSColumn.class)) {
                continue;
            }
            final String foreignColumnName = getColumnName(foreignMethod);
            if (foreignColumnName.equals(foreignColumnName)) {
                foreignKeyExists = true;
                foreignKeyType = foreignMethod.getGenericReturnType();
                break;
            }
        }
        if (!foreignKeyExists) {
            throw new IllegalArgumentException("method " + method.getName() + " references foreign method (" + foreignTableName + "." + foreignKey.columnName() + ") that does not exist");
        }
        if (!method.getGenericReturnType().equals(foreignKeyType)) {
            throw new IllegalArgumentException("field " + method.getName() + " references foreign field (" + foreignTableName + "." + foreignKey.columnName() + ") that exists, but is of incorrect type");
        }
    }

    // FOR HANDLING STATIC DATA

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


    // Private classes

    private static enum TypeTranslator {
        LONG(long.class, "INTEGER"),
        STRING(String.class, "TEXT");

        private Type type;
        private String sqliteTypeString;

        private TypeTranslator(Type type, String sqliteTypeString) {
            this.type = type;
            this.sqliteTypeString = sqliteTypeString;
        }

        public static TypeTranslator getFrom(Type type) {
            for (TypeTranslator typeTranslator : TypeTranslator.values()) {
                if (typeTranslator.getType().equals(type)) {
                    return typeTranslator;
                }
            }
            return null;
        }

        public Type getType() {
            return type;
        }

        public String getSQLiteTypeString() {
            return sqliteTypeString;
        }
    }
}
