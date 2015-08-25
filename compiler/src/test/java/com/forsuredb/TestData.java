package com.forsuredb;

import com.forsuredb.annotationprocessor.ColumnInfo;
import com.forsuredb.annotationprocessor.TableInfo;
import com.forsuredb.migration.sqlite.TypeTranslator;

public class TestData {

    public static final ColumnInfo[] DEFAULT_COLUMNS = new ColumnInfo[] {TestData.idCol(), TestData.createdCol(), TestData.deletedCol(), TestData.modifiedCol()};

    // Convenience constants
    public static final String TABLE_NAME = "test_table";

    // Convenience methods for making data to go into the tests
    public static TableInfo.Builder table() {
        return TableInfo.builder().tableName(TABLE_NAME);
    }

    public static ColumnInfo idCol() {
        return ColumnInfo.builder().columnName("_id")
                .qualifiedType(TypeTranslator.LONG.getQualifiedType())
                .primaryKey(true)
                .build();
    }

    public static ColumnInfo createdCol() {
        return ColumnInfo.builder().columnName("created")
                .qualifiedType(TypeTranslator.DATE.getQualifiedType())
                .defaultValue("CURRENT_TIMESTAMP")
                .build();
    }

    public static ColumnInfo deletedCol() {
        return ColumnInfo.builder().columnName("deleted")
                .qualifiedType(TypeTranslator.BOOLEAN.getQualifiedType())
                .defaultValue("0")
                .build();
    }

    public static ColumnInfo modifiedCol() {
        return ColumnInfo.builder().columnName("modified")
                .qualifiedType(TypeTranslator.DATE.getQualifiedType())
                .defaultValue("CURRENT_TIMESTAMP")
                .build();
    }

    public static ColumnInfo.Builder stringCol() {
        return columnFrom(TypeTranslator.STRING);
    }

    public static ColumnInfo.Builder intCol() {
        return columnFrom(TypeTranslator.INT);
    }

    public static ColumnInfo.Builder longCol() {
        return columnFrom(TypeTranslator.LONG);
    }

    public static ColumnInfo.Builder doubleCol() {
        return columnFrom(TypeTranslator.DOUBLE);
    }

    public static ColumnInfo.Builder booleanCol() {
        return columnFrom(TypeTranslator.BOOLEAN);
    }

    public static ColumnInfo.Builder bigDecimalCol() {
        return columnFrom(TypeTranslator.BIG_DECIMAL);
    }

    public static ColumnInfo.Builder dateCol() {
        return columnFrom(TypeTranslator.DATE);
    }

    // Helpers for covenience methods

    private static ColumnInfo.Builder columnFrom(TypeTranslator tt) {
        return ColumnInfo.builder().columnName(nameFrom(tt)).qualifiedType(tt.getQualifiedType());
    }

    private static String nameFrom(TypeTranslator tt) {
        return tt.name().toLowerCase() + "_column";
    }
}
