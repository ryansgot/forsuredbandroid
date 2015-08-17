package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.QueryGenerator;
import com.forsuredb.migration.sqlite.AddColumnGenerator;
import com.forsuredb.migration.sqlite.AddForeignKeyGenerator;
import com.forsuredb.migration.sqlite.CreateTableGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class XmlGenerator {

    private static final String TAG_NAME = "migration";

    private final int dbVersion;
    private final PriorityQueue<QueryGenerator> queryGenerators;

    /*package*/ XmlGenerator(int dbVersion, List<TableInfo> allTables) {
        this.dbVersion = dbVersion;
        queryGenerators = createOrderedQueryGenerators(allTables);
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
            for (String query : queryGenerator.generate()) {
                retList.add(new StringBuffer("<").append(TAG_NAME).append(" db_version=\"").append(dbVersion)
                        .append("\" db_type=\"").append(dbType.asString())
                        .append("\" table_name=\"").append(queryGenerator.getTableName())
                        .append("\" migration_type=\"").append(queryGenerator.getMigrationType().toString())
                        .append("\" query=\"").append(performXmlReplacements(query))
                        .append("\" />").toString());
            }
        }

        return retList;
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

    private PriorityQueue<QueryGenerator> createOrderedQueryGenerators(List<TableInfo> allTables) {
        PriorityQueue<QueryGenerator> queue = new PriorityQueue<>();
        for (TableInfo table : allTables) {
            queue.addAll(createQueryGenerators(table));
        }
        return queue;
    }

    private List<QueryGenerator> createQueryGenerators(TableInfo table) {
        List<QueryGenerator> retList = new LinkedList<>();
        retList.add(new CreateTableGenerator(table.getTableName()));
        for (ColumnInfo column : table.getColumns()) {
            if ("_id".equals(column.getColumnName())) {
                continue;   // <-- don't ever add the _id column because it's on the table create
            }

            if (column.isForeignKey()) {
                retList.add(new AddForeignKeyGenerator(table, column));
            } else {
                retList.add(new AddColumnGenerator(table.getTableName(), column));
            }
        }

        return retList;
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
