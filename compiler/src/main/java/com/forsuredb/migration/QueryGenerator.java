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
package com.forsuredb.migration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class QueryGenerator implements Comparable<QueryGenerator> {

    private final MigrationType type;
    private final String tableName;

    public QueryGenerator(String tableName, MigrationType type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("tableName cannot be null or empty");
        }
        this.type = type;
        this.tableName = tableName;
    }

    @Override
    public final int compareTo(QueryGenerator other) {
        if (other == null) {
            return -1;
        }
        return type.getPriority() - other.getMigrationType().getPriority();
    }

    public abstract List<String> generate();

    public final MigrationType getMigrationType() {
        return type;
    }

    public final String getTableName() {
        return tableName;
    }

    public Map<String, String> getAdditionalAttributes() {
        return Collections.EMPTY_MAP;
    }

    public enum MigrationType {
        CREATE_TABLE(0),
        ALTER_TABLE_ADD_COLUMN(1),
        ALTER_TABLE_ADD_UNIQUE(1),
        ADD_FOREIGN_KEY_REFERENCE(2),
        ADD_UNIQUE_INDEX(3),
        CREATE_TEMP_TABLE_FROM_EXISTING(4),
        DROP_TABLE(5);

        private int priority;

        MigrationType(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public static MigrationType from(String name) {
            for (MigrationType type : MigrationType.values()) {
                if (type.name().equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }
}
