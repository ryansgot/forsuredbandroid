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
