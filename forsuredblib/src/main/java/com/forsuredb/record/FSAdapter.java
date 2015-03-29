package com.forsuredb.record;

import android.database.Cursor;

import com.google.common.collect.ImmutableMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

public class FSAdapter {

    private static ImmutableMap<Type, Method> cursorMethodMap;
    static {
        try {
            cursorMethodMap = new ImmutableMap.Builder<Type, Method>().put(long.class, Cursor.class.getDeclaredMethod("getLong", int.class))
                                                                      .put(boolean.class, Cursor.class.getDeclaredMethod("getInt", int.class))
                                                                      .put(String.class, Cursor.class.getDeclaredMethod("getString", int.class))
                                                                      .build();
        } catch (NoSuchMethodException nsme) {
        }
    }

    private static final Handler handler = new Handler();

    private final Class<? extends FSApi> tableApi;

    private FSAdapter(Class<? extends FSApi> tableApi) {
        this.tableApi = tableApi;
    }

    public static <T> T create(Class<T> tableApi) {
        validateTableApi(tableApi);
        return (T) Proxy.newProxyInstance(tableApi.getClassLoader(), new Class<?>[] {tableApi}, handler);
    }

    private static <T> void validateTableApi(Class<T> tableApi) {
        for (Method m : tableApi.getDeclaredMethods()) {
            if (!cursorMethodMap.containsKey(m.getGenericReturnType())) {
                throw new IllegalArgumentException("method " + m.getName() + " has illegal return type, supported types are: " + cursorMethodMap.keySet().toString());
            }

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

    private static class Handler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            if (args[0] == null || !args[0].getClass().equals(Cursor.class)) {
                throw new IllegalArgumentException("You must pass an object of the Cursor class");
            }
            return cursorMethodMap.get(method.getGenericReturnType()).invoke(proxy, args);
        }
    }
}
