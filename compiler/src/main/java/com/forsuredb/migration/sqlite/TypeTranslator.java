package com.forsuredb.migration.sqlite;

import java.math.BigDecimal;

import javax.lang.model.type.TypeMirror;

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
    private String sqlString;

    TypeTranslator(String typeMirrorString, String sqlString) {
        this.typeMirrorString = typeMirrorString;
        this.sqlString = sqlString;
    }

    public static TypeTranslator from(TypeMirror typeMirror) {
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

    public static TypeTranslator from(String qualifiedTypeString) {
        if (qualifiedTypeString == null) {
            return null;
        }

        for (TypeTranslator typeTranslator : TypeTranslator.values()) {
            if (typeTranslator.getTypeMirrorString().equals(qualifiedTypeString)) {
                return typeTranslator;
            }
        }

        return STRING;
    }

    public String getTypeMirrorString() {
        return typeMirrorString;
    }

    public String getSqlString() {
        return sqlString;
    }
}
