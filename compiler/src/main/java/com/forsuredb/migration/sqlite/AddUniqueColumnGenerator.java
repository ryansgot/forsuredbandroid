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
package com.forsuredb.migration.sqlite;

import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.migration.QueryGenerator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AddUniqueColumnGenerator extends QueryGenerator {


    private final ColumnInfo column;

    public AddUniqueColumnGenerator(String tableName, ColumnInfo column) {
        super(tableName, MigrationType.ALTER_TABLE_ADD_UNIQUE);
        this.column = column;
    }

    @Override
    public Map<String, String> getAdditionalAttributes() {
        Map<String, String> ret = new HashMap<>();
        ret.put("column", column.getColumnName());
        ret.put("column_type", column.getQualifiedType());
        return ret;
    }

    @Override
    public List<String> generate() {
        List<String> retList = new LinkedList<>();
        retList.addAll(new AddColumnGenerator(getTableName(), column).generate());
        retList.add("CREATE UNIQUE INDEX " + getTableName() + "_" + column.getColumnName() + " ON " + getTableName() + "(" + column.getColumnName() + ");");
        return retList;
    }
}
