package com.forsuredb.record;

import android.database.Cursor;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/*package*/ class ApiValidator {

    public static <T> void validate(Class<T> tableApi) {
        for (Method m : tableApi.getDeclaredMethods()) {
            validateReturn(m);
            validateParameters(m);
        }
    }

    private static void validateReturn(Method m) {
        if (!FSAdapter.cursorMethodMap.containsKey(m.getGenericReturnType())) {
            throw new IllegalArgumentException("method " + m.getName() + " has illegal return type, supported types are: " + FSAdapter.cursorMethodMap.keySet().toString());
        }
    }

    private static void validateParameters(Method m) {
        Type[] types = m.getParameterTypes();
        if (types.length > 1) {
            throw new IllegalArgumentException("method " + m.getName() + " has more than one parameter. Only one Cursor parameter is allowed");
        }
        if (types.length < 1) {
            throw new IllegalArgumentException("method " + m.getName() + " has less than one parameter. A Cursor parameter is required.");
        }
        if (!types[0].equals(Cursor.class)) {
            throw new IllegalArgumentException("method " + m.getName() + " has a " + types[0].getClass().getName() + " parameter. A Cursor parameter is required.");
        }
    }
}
