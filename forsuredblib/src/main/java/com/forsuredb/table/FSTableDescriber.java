package com.forsuredb.table;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.net.Uri;

import com.forsuredb.record.As;
import com.forsuredb.record.FSRecordModel;
import com.forsuredb.record.ForeignKey;
import com.forsuredb.record.PrimaryKey;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class FSTableDescriber {

    private static final int NO_STATIC_DATA_RESOURCE_ID = -1;

    private final String name;
    private final Class<? extends FSRecordModel> recordModelClass;
    private final int staticDataResourceId;
    private final String staticDataRecordName;
    private final String mimeType;
    private final Uri allRecordsUri;

    private String tableCreateQuery;

    public FSTableDescriber(String authority, Class<? extends FSRecordModel> recordModelClass, int staticDataResourceId, String staticDataRecordName) throws IllegalStateException {
        validate(authority, recordModelClass);
        this.name = recordModelClass.getAnnotation(FSTable.class).value();
        this.recordModelClass = recordModelClass;
        this.staticDataResourceId = staticDataResourceId;
        this.staticDataRecordName = staticDataRecordName;
        mimeType = "vnd.android.cursor/" + name;
        allRecordsUri = Uri.parse("content://" + authority + "/" + name);
        ForSure.getInstance().putTable(this);
    }

    public FSTableDescriber(String authority, Class<? extends FSRecordModel> recordModelClass) throws IllegalStateException {
        this(authority, recordModelClass, NO_STATIC_DATA_RESOURCE_ID, "");
    }

    public String getName() {
        return name;
    }

    public Class<? extends FSRecordModel> getRecordModelClass() {
        return recordModelClass;
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

    /**
     * <p>
     *     Returns a list of the insert statements that will be used to populate the static data in this table
     * </p>
     *
     * @return an empty list if there should be no static data
     */
    public List<String> getStaticInsertsSQL(Context context) {
        if (staticDataResourceId == NO_STATIC_DATA_RESOURCE_ID || Strings.isNullOrEmpty(staticDataRecordName)) {
            return Collections.EMPTY_LIST;
        }

        final String queryPrefix = "INSERT INTO " + name + " (";
        XmlResourceParser parser = context.getResources().getXml(staticDataResourceId);
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

        for (Field field : recordModelClass.getDeclaredFields()) {
            appendColumnDefinitionTo(queryBuffer, field);
            queryBuffer.append(", ");
        }
        queryBuffer.delete(queryBuffer.length() - 2, queryBuffer.length()); // <-- remove final ", "
        appendForeignKeysLineTo(queryBuffer);
        return queryBuffer.append(");").toString();
    }

    private void appendColumnDefinitionTo(StringBuffer queryBuffer, Field field) {
        final TypeTranslator typeTranslator = TypeTranslator.getFrom(field.getGenericType());
        queryBuffer.append(getColumnName(field)).append(" ")
                   .append(typeTranslator == null ? "" : typeTranslator.getSQLiteTypeString())
                   .append(field.isAnnotationPresent(PrimaryKey.class) ? " " + field.getAnnotation(PrimaryKey.class).definitionText() : "");
    }

    private void appendForeignKeysLineTo(StringBuffer queryBuffer) {
        for (Field field : recordModelClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ForeignKey.class)) {
                continue;
            }
            final String columnName = field.isAnnotationPresent(As.class) ? field.getAnnotation(As.class).value() : field.getName();
            final ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
            queryBuffer.append(", FOREIGN KEY(")
                       .append(columnName)
                       .append(") REFERENCES ")
                       .append(foreignKey.tableName())
                       .append("(").append(foreignKey.columnName())
                       .append(")");
        }
    }

    private String getColumnName(Field field) {
        return field.isAnnotationPresent(As.class) ? field.getAnnotation(As.class).value() : field.getName();
    }


    /**
     * <p>
     *     Validates the properties of this table to be correct
     * </p>
     */
    private void validate(String authority, Class<? extends FSRecordModel> recordModelClass) throws IllegalStateException, IllegalArgumentException {
        if (!recordModelClass.isAnnotationPresent(FSTable.class)) {
            throw new IllegalArgumentException("Cannot create table without a table name. Use the FSTable annotation on all FSDataModel extensions");
        }
        final String name = recordModelClass.getAnnotation(FSTable.class).value();
        if (ForSure.getInstance().containsTable(name)) {
            throw new IllegalArgumentException("Cannot create table named " + name + "; that table already exists.");
        }
        if (authority == null) {
            throw new IllegalArgumentException ("Cannot create table with null authority");
        }
        for (Field field : recordModelClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ForeignKey.class)) {
                continue;
            }
            final ForeignKey foreignKey = field.getAnnotation(ForeignKey.class);
            validateForeignKeyRelationship(field, foreignKey);
        }
    }

    private void validateForeignKeyRelationship(Field field, ForeignKey foreignKey) throws IllegalStateException, IllegalArgumentException {
        final ForSure forSure = ForSure.getInstance();
        if (!forSure.containsTable(foreignKey.tableName())) {
            throw new IllegalStateException("Must create table " + foreignKey.tableName() + " prior to creating table " + name);
        }

        Class<? extends FSRecordModel> foreignRecordModelClass = forSure.getTable(foreignKey.tableName()).getRecordModelClass();
        boolean foreignKeyExists = false;
        Type foreignKeyType = null;
        for (Field foreignField : foreignRecordModelClass.getDeclaredFields()) {
            final String foreignColumnName = getColumnName(foreignField);
            if (foreignKey.columnName().equals(foreignColumnName)) {
                foreignKeyExists = true;
                foreignKeyType = foreignField.getType();
                break;
            }
        }
        if (!foreignKeyExists) {
            throw new IllegalArgumentException("field " + field.getName() + " references foreign field (" + foreignKey.tableName() + "." + foreignKey.columnName() + ") that does not exist");
        }
        if (!field.getType().equals(foreignKeyType)) {
            throw new IllegalArgumentException("field " + field.getName() + " references foreign field (" + foreignKey.tableName() + "." + foreignKey.columnName() + ") that exists, but is of incorrect type");
        }
    }

    // FOR HANDLING STATIC DATA

    private String getInsertionQuery(XmlResourceParser parser, String queryPrefix) {
        final StringBuffer queryBuf = new StringBuffer(queryPrefix);
        final StringBuffer valueBuf = new StringBuffer();
        for (Field field : recordModelClass.getDeclaredFields()) {
            final String columnName = getColumnName(field);
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
        LONG(Long.class, "INTEGER"),
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
