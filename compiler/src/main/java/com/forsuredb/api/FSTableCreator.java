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
package com.forsuredb.api;

import com.forsuredb.annotation.FSTable;

public class FSTableCreator implements Comparable<FSTableCreator> {

    private static final String NO_STATIC_DATA_ASSET = "";

    private final String authority;
    private final Class<? extends FSGetApi> tableApiClass;
    private final String staticDataAsset;
    private final String staticDataRecordName;
    private final Class<? extends FSGetApi>[] foreignKeyClasses;
    private final String tableName;

    public FSTableCreator(String authority, Class<? extends FSGetApi> tableApiClass, String staticDataAsset, String staticDataRecordName, Class<? extends FSGetApi>... foreignKeyClasses) {
        this.authority = authority;
        this.tableApiClass = tableApiClass;
        this.staticDataAsset = staticDataAsset;
        this.staticDataRecordName = staticDataRecordName;
        this.foreignKeyClasses = foreignKeyClasses;
        this.tableName = tableApiClass.getAnnotation(FSTable.class).value();
    }

    public FSTableCreator(String authority, Class<? extends FSGetApi> tableApiClass, Class<? extends FSGetApi>... foreignKeyClasses) {
        this(authority, tableApiClass, NO_STATIC_DATA_ASSET, "", foreignKeyClasses);
    }

    @Override
    public int compareTo(FSTableCreator o) {
        if (o == null) {
            return -1;
        }
        if (hasForeignKeyTo(o.getTableApiClass())) {
            return 1;
        }
        if (o.hasForeignKeyTo(tableApiClass)) {
            return -1;
        }
        return 0;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAuthority() {
        return authority;
    }

    public Class<? extends FSGetApi> getTableApiClass() {
        return tableApiClass;
    }

    public String getStaticDataAsset() {
        return staticDataAsset;
    }

    public String getStaticDataRecordName() {
        return staticDataRecordName;
    }

    private boolean hasForeignKeyTo(Class<? extends FSGetApi> otherTableApi) {
        if (otherTableApi == null) {
            return false;
        }

        for (Class<? extends FSGetApi> foreignKeyClass : foreignKeyClasses) {
            if (foreignKeyClass.equals(otherTableApi)) {
                return true;
            }
        }

        return false;
    }
}
