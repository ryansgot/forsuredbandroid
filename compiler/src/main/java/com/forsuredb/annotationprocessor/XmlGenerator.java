package com.forsuredb.annotationprocessor;

import java.math.BigDecimal;
import java.util.List;

import javax.lang.model.type.TypeMirror;

public abstract class XmlGenerator {

    private static final String TAG_NAME = "migration";

    private final int dbVersion;
    private final String tableName;
    private final Keyword keyword;

    /*package*/ XmlGenerator(int dbVersion, String tableName, Keyword keyword) {
        this.dbVersion = dbVersion;
        this.tableName = tableName;
        this.keyword = keyword;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * <p>
     *     Generates the XML which contains the definition of the Migration
     * </p>
     *
     * @param dbType
     * @return
     */
    public final String generate(DBType dbType, List<TableInfo> allTables) {
        if (!canGenerate(dbType)) {
            return "\n";
        }
        return new StringBuffer("<").append(TAG_NAME).append(" db_version=\"").append(dbVersion)
                                    .append("\" table_name=\"").append(tableName)
                                    .append("\" keyword=\"").append(keyword.name())
                                    .append("\" query=\"").append(performXmlReplacements(generateQuery(dbType, allTables)))
                                    .append("\" />").toString();
    }

    protected abstract String generateQuery(DBType dbType, List<TableInfo> allTables);

    private String performXmlReplacements(String attribute) {
        if (attribute == null) {
            return "";
        }
        return attribute.replace("&", "&amp;")
                        .replace("'", "&apos;")
                        .replace("\"", "&quot;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");
    }

    private boolean canGenerate(DBType dbType) {
        return dbVersion > 0 && tableName != null && !tableName.isEmpty() && keyword != null && dbType != null;
    }

    public static class Builder {

        private int dbVersion = 1;
        private TableInfo tableInfo;
        private Keyword keyword;

        private Builder() {}

        public Builder dbVersion(int dbVersion) {
            this.dbVersion = dbVersion < 1 ? this.dbVersion : dbVersion;
            return this;
        }

        public Builder tableInfo(TableInfo tableInfo) {
            this.tableInfo = tableInfo;
            return this;
        }

        public Builder keyword(Keyword keyword) {
            this.keyword = keyword;
            return this;
        }

        public XmlGenerator build() {
            if (keyword == null) {
                return new EmptyLineGenerator();
            }

            switch (keyword) {
                case CREATE:
                    return new TableCreateGenerator(dbVersion, tableInfo, keyword);
                // TODO: add more cases for the various keywords supported
            }

            return new EmptyLineGenerator();
        }
    }

    public enum Keyword {
        CREATE("CREATE TABLE");
        // TODO: add support for more

        private String sqlite;

        Keyword(String sqlite) {
            this.sqlite = sqlite;
        }

        public String getSqlite() {
            return sqlite;
        }
    }

    public enum ColumnDescriptor {
        AUTOINCREMENT("AUTOINCREMENT"),
        PRIMARY_KEY("PRIMARY KEY"),
        REFERENCES("REFERENCES"),
        UNIQUE("UNIQUE");

        private String sqliteDefinition;

        ColumnDescriptor(String sqliteDefinition) {
            this.sqliteDefinition = sqliteDefinition;
        }

        public String getSqliteDefinition() {
            return sqliteDefinition;
        }
    }

    public enum DBType {
        SQLITE("sqlite");
        // TODO: add support for more database types here

        private String str;

        DBType(String str) {
            this.str = str;
        }

        public static DBType fromString(String str) {
            for (DBType dbType : DBType.values()) {
                if (dbType.asString().equalsIgnoreCase(str)) {
                    return dbType;
                }
            }
            return SQLITE;
        }

        public String asString() {
            return str;
        }
    }

    public enum TypeTranslator {
        BIG_DECIMAL(BigDecimal.class.getName(), "REAL"),
        BOOLEAN("boolean", "INTEGER"),
        BOOLEAN_WRAPPER(Boolean.class.getName(), "INTEGER"),
        BYTE_ARRAY("byte[]", "BLOB"),
        DOUBLE("double", "REAL"),
        DOUBLE_WRAPPER(Double.class.getName(), "REAL"),
        FLOAT("float", "REAL"),
        FLOAT_WRAPPER(Float.class.getName(), "REAL"),
        INT("int", "INTEGER"),
        INT_WRAPPER(Integer.class.getName(), "INTEGER"),
        LONG("long", "INTEGER"),
        LOG_WRAPPER(Long.class.getName(), "INTEGER"),
        STRING(String.class.getName(), "TEXT");

        private String typeMirrorString;
        private String sqliteTypeString;

        TypeTranslator(String typeMirrorString, String sqliteTypeString) {
            this.typeMirrorString = typeMirrorString;
            this.sqliteTypeString = sqliteTypeString;
        }

        public static TypeTranslator getFrom(TypeMirror typeMirror) {
            if (typeMirror == null) {
                return null;
            }

            for (TypeTranslator typeTranslator : TypeTranslator.values()) {
                if (typeTranslator.getTypeMirrorString().equals(typeMirror.toString())) {
                    return typeTranslator;
                }
            }

            return STRING;
        }

        public String getTypeMirrorString() {
            return typeMirrorString;
        }

        public String getSQLiteTypeString() {
            return sqliteTypeString;
        }
    }
}
