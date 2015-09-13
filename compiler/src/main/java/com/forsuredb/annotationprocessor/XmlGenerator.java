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
package com.forsuredb.annotationprocessor;

import com.forsuredb.migration.Migration;
import com.forsuredb.migration.QueryGenerator;

import java.util.ArrayList;
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
     *     Generates the XML which contains the representation of all of the
     *     {@link Migration Migration} objects for each {@link QueryGenerator QueryGenerator} in the
     *     priority queue.
     * </p>
     *
     * @return a List&lt;String&gt; of the generated XML representing {@link Migration Migration}
     * objects
     */
    public final List<String> generate() {
        List<String> retList = new ArrayList<>();
        while (queryGenerators.size() > 0) {
            QueryGenerator queryGenerator = queryGenerators.remove();
            List<String> queries = queryGenerator.generate();
            while (queries.size() > 0) {
                String query = queries.remove(0);
                StringBuffer lineBuf = beginLine(queryGenerator, query);
                if (queries.size() == 0) {
                    appendAdditionalAttributes(lineBuf, queryGenerator);
                    lineBuf.append("\" is_last_in_set=\"true");
                }
                retList.add(lineBuf.append("\" />").toString());
            }
        }

        return retList;
    }

    private StringBuffer beginLine(QueryGenerator queryGenerator, String query) {
        return new StringBuffer("<").append(TAG_NAME).append(" db_version=\"").append(dbVersion)
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
        return attribute.replace("&", "&amp;").replace("'", "&apos;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
