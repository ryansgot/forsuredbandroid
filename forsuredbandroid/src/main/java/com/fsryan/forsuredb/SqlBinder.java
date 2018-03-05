package com.fsryan.forsuredb;

import android.content.ContentValues;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fsryan.forsuredb.api.RecordContainer;

import java.util.List;

public abstract class SqlBinder {

    public static void bindObjects(@NonNull SQLiteStatement s, @NonNull List<String> cols, @NonNull RecordContainer rc) {
        for (int pos = 0; pos < cols.size(); pos ++) {
            bindObject(pos + 1, s, rc.get(cols.get(pos)));
        }
    }

    public static void bindObjects(@NonNull SQLiteStatement s, @NonNull List<String> cols, @NonNull ContentValues cv) {
        for (int pos = 0; pos < cols.size(); pos ++) {
            bindObject(pos + 1, s, cv.get(cols.get(pos)));
        }
    }

    public static void bindObjects(@NonNull SQLiteStatement s, @Nullable Object[] objects) {
        bindObjects(s, 1, objects);
    }

    public static void bindObjects(@NonNull SQLiteStatement s, int startPos, @Nullable Object[] objects) {
        if (objects == null) {
            return;
        }

        for (int idx = 0; idx < objects.length; idx++) {
            bindObject(startPos + idx, s, objects[idx]);
        }
    }

    public static void bindObjects(@NonNull SQLiteQuery q, @Nullable Object[] objects) {
        bindObjects(q, 1, objects);
    }

    public static void bindObjects(@NonNull SQLiteQuery q, int startPos, @Nullable Object[] objects) {
        if (objects == null) {
            return;
        }

        for (int idx = 0; idx < objects.length; idx++) {
            bindObject(startPos + idx, q, objects[idx]);
        }
    }

    public static void bindObject(int pos, SQLiteStatement s, Object obj) {
        Class<?> cls = obj.getClass();
        if (cls == Long.class) {
            s.bindLong(pos, (long) obj);
        } else if (cls == Integer.class) {
            s.bindLong(pos, (int) obj);
        } else if (cls == Double.class) {
            s.bindDouble(pos, (double) obj);
        } else if (cls == Float.class) {
            s.bindDouble(pos, (float) obj);
        } else if (cls == String.class) {
            s.bindString(pos, (String) obj);
        } else if (cls == byte[].class) {
            s.bindBlob(pos, (byte[]) obj);
        } else if (cls == Boolean.class) {
            s.bindLong(pos, ((boolean) obj) ? 1L : 0L);
        } else {
            throw new IllegalArgumentException("Cannot bind object of type: " + cls);
        }
    }

    public static void bindObject(int pos, SQLiteQuery q, Object obj) {
        Class<?> cls = obj.getClass();
        if (cls == Long.class) {
            q.bindLong(pos, (long) obj);
        } else if (cls == Integer.class) {
            q.bindLong(pos, (int) obj);
        } else if (cls == Double.class) {
            q.bindDouble(pos, (double) obj);
        } else if (cls == Float.class) {
            q.bindDouble(pos, (float) obj);
        } else if (cls == String.class) {
            q.bindString(pos, (String) obj);
        } else if (cls == byte[].class) {
            q.bindBlob(pos, (byte[]) obj);
        } else if (cls == Boolean.class) {
            q.bindLong(pos, ((boolean) obj) ? 1L : 0L);
        } else {
            throw new IllegalArgumentException("Cannot bind object of type: " + cls);
        }
    }
}
