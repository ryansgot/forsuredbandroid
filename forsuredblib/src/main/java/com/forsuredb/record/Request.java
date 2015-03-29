package com.forsuredb.record;

import android.database.Cursor;
import android.util.Log;

import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Request<T> {

    private static ImmutableMap<Class, Method> cursorMethodMap;
    static {
        try {
            cursorMethodMap = new ImmutableMap.Builder<Class, Method>().put(Long.class, Cursor.class.getDeclaredMethod("getLong", int.class))
                                                                       .put(String.class, Cursor.class.getDeclaredMethod("getString", int.class))
                                                                       .build();
        } catch (NoSuchMethodException nsme) {
        }
    }

    private final String columnName;
    private final Class<T> clazz;

    private Request(String columnName, Class<T> clazz) {
        this.columnName = columnName;
        this.clazz = clazz;
    }

    public static <T> Request<T> get(String columnName, Class<T> clazz) {
        return new Request<T>(columnName, clazz);
    }

    public T from(Cursor cursor) {
        if (clazz == null) {
            return null;
        }
        try {
            final Constructor<T> constructor = getConstructor(clazz);
            final Method cursorMethod = cursorMethodMap.get(clazz);
            final Object arg = cursorMethod.invoke(cursor, cursor.getColumnIndex(columnName));
            return constructor.newInstance(arg);
        } catch (Exception e) {
            Log.e("Request", "could not get column " + columnName + " from cursor");
            e.printStackTrace();
        }
        return null;
    }

    private Constructor<T> getConstructor(Class<T> clazz) throws NoSuchMethodException {
        if (Long.class.equals(clazz)) {
            return clazz.getConstructor(long.class);
        }
        if (String.class.equals(clazz)) {
            return clazz.getConstructor(String.class);
        }
        return null;
    }
}
