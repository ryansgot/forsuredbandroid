package com.fsryan.forsuredb;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;

import com.fsryan.forsuredb.api.RecordContainer;

import java.util.List;

public abstract class StatementBinder {

    public static void bindObjects(SQLiteStatement pStatement, List<String> columns, RecordContainer recordContainer) {
        for (int pos = 0; pos < columns.size(); pos ++) {
            bindObject(pos + 1, pStatement, recordContainer.get(columns.get(pos)));
        }
    }

    public static void bindObjects(SQLiteStatement pStatement, List<String> columns, ContentValues cv) {
        for (int pos = 0; pos < columns.size(); pos ++) {
            bindObject(pos + 1, pStatement, cv.get(columns.get(pos)));
        }
    }

    public static void bindObject(int idx, SQLiteStatement pStatement, Object obj) {
        Class<?> cls = obj.getClass();
        if (cls == Long.class) {
            pStatement.bindLong(idx, (long) obj);
        } else if (cls == Integer.class) {
            pStatement.bindLong(idx, (int) obj);
        } else if (cls == Double.class) {
            pStatement.bindDouble(idx, (double) obj);
        } else if (cls == Float.class) {
            pStatement.bindDouble(idx, (float) obj);
        } else if (cls == String.class) {
            pStatement.bindString(idx, (String) obj);
        } else if (cls == byte[].class) {
            pStatement.bindBlob(idx, (byte[]) obj);
        } else if (cls == Boolean.class) {
            pStatement.bindLong(idx, ((boolean) obj) ? 1L : 0L);
        } else {
            throw new IllegalArgumentException("Cannot bind object of type: " + cls);
        }
    }
}
