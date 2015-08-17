package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.QueryGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class XmlGenerator {

    private static final String TAG_NAME = "migration";

    private final int dbVersion;
    private final PriorityQueue<QueryGenerator> queryGenerators;

    /*package*/ XmlGenerator(int dbVersion, PriorityQueue<QueryGenerator> queryGenerators) {
        this.dbVersion = dbVersion;
        this.queryGenerators = queryGenerators;
    }

    /**
     * <p>
     *     Generates the XML which contains the definition of the Migration
     * </p>
     *
     * @param dbType
     * @return
     */
    public final List<String> generate(DBType dbType) {
        if (!canGenerate(dbType)) {
            return Collections.EMPTY_LIST;
        }

        List<String> retList = new ArrayList<>();
        while (queryGenerators.size() > 0) {
            QueryGenerator queryGenerator = queryGenerators.remove();
            List<String> queries = queryGenerator.generate();
            while (queries.size() > 0) {
                String query = queries.remove(0);
                StringBuffer lineBuf = beginLine(dbType, queryGenerator, query);
                if (queries.size() == 0) {
                    appendAdditionalAttributes(lineBuf, queryGenerator);
                    lineBuf.append("\" is_last_in_set=\"true");
                }
                retList.add(lineBuf.append("\" />").toString());
            }
        }

        return retList;
    }

    private StringBuffer beginLine(DBType dbType, QueryGenerator queryGenerator, String query) {
        return new StringBuffer("<").append(TAG_NAME).append(" db_version=\"").append(dbVersion)
                .append("\" db_type=\"").append(dbType.asString())
                .append("\" table_name=\"").append(queryGenerator.getTableName())
                .append("\" migration_type=\"").append(queryGenerator.getMigrationType().toString())
                .append("\" query=\"").append(performXmlReplacements(query));
    }

    private void appendAdditionalAttributes(StringBuffer lineBuf, QueryGenerator queryGenerator) {
        for (Map.Entry<String, String> entry : queryGenerator.getAdditionalAttributes().entrySet()) {
            lineBuf.append("\" ").append(entry.getKey()).append("=\"").append(performXmlReplacements(entry.getValue()));
        }
    }

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
        return dbVersion > 0 && dbType != null;
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
}
