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

import com.forsuredb.migration.QueryGenerator;

import java.util.LinkedList;
import java.util.List;

public class DropTableGenerator extends QueryGenerator {

    public DropTableGenerator(String tableName) {
        super(tableName, MigrationType.DROP_TABLE);
    }

    @Override
    public List<String> generate() {
        List<String> retList = new LinkedList<>();
        retList.add("DROP TABLE IF EXISTS " + getTableName() + ";");
        return retList;
    }
}
