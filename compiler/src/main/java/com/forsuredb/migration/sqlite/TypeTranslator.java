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

import java.math.BigDecimal;
import java.util.Date;

import javax.lang.model.type.TypeMirror;

public enum TypeTranslator {
    BIG_DECIMAL(BigDecimal.class.getName(), "REAL"),
    BOOLEAN("boolean", "INTEGER"),
    BOOLEAN_WRAPPER(Boolean.class.getName(), "INTEGER"),
    BYTE_ARRAY("byte[]", "BLOB"),
    DATE(Date.class.getName(), "DATETIME"),
    DOUBLE("double", "REAL"),
    DOUBLE_WRAPPER(Double.class.getName(), "REAL"),
    FLOAT("float", "REAL"),
    FLOAT_WRAPPER(Float.class.getName(), "REAL"),
    INT("int", "INTEGER"),
    INT_WRAPPER(Integer.class.getName(), "INTEGER"),
    LONG("long", "INTEGER"),
    LOG_WRAPPER(Long.class.getName(), "INTEGER"),
    STRING(String.class.getName(), "TEXT");

    private String qualifiedType;
    private String sqlString;

    TypeTranslator(String qualifiedType, String sqlString) {
        this.qualifiedType = qualifiedType;
        this.sqlString = sqlString;
    }

    public static TypeTranslator from(TypeMirror typeMirror) {
        if (typeMirror == null) {
            return null;
        }

        for (TypeTranslator typeTranslator : TypeTranslator.values()) {
            if (typeTranslator.getQualifiedType().equals(typeMirror.toString())) {
                return typeTranslator;
            }
        }

        return STRING;
    }

    public static TypeTranslator from(String qualifiedTypeString) {
        if (qualifiedTypeString == null) {
            return null;
        }

        for (TypeTranslator typeTranslator : TypeTranslator.values()) {
            if (typeTranslator.getQualifiedType().equals(qualifiedTypeString)) {
                return typeTranslator;
            }
        }

        return STRING;
    }

    public String getQualifiedType() {
        return qualifiedType;
    }

    public String getSqlString() {
        return sqlString;
    }
}
