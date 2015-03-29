package com.forsuredb.record;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.google.common.collect.ImmutableMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

public class FSAdapter {

    /*package*/ static ImmutableMap<Type, Method> cursorMethodMap;
    static {
        try {
            cursorMethodMap = new ImmutableMap.Builder<Type, Method>().put(long.class, Cursor.class.getDeclaredMethod("getLong", int.class))
                                                                      .put(boolean.class, Cursor.class.getDeclaredMethod("getInt", int.class))
                                                                      .put(String.class, Cursor.class.getDeclaredMethod("getString", int.class))
                                                                      .build();
        } catch (NoSuchMethodException nsme) {
        }
    }

    private static final Handler handler = new Handler();   // <-- there only needs to be one handler ever

    public static <T> T create(Class<T> tableApi) {
        ApiValidator.validate(tableApi);
        return (T) Proxy.newProxyInstance(tableApi.getClassLoader(), new Class<?>[] {tableApi}, handler);
    }

    private static class Handler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            if (args[0] == null || !(args[0] instanceof CursorWrapper)) {
                throw new IllegalArgumentException("You must pass an object of the Cursor class as the first argument, passed: " + args[0].getClass().getName());
            }
            FSColumn fsColumn = method.getAnnotation(FSColumn.class);
            return cursorMethodMap.get(method.getGenericReturnType()).invoke(args[0], ((CursorWrapper) args[0]).getColumnIndex(fsColumn.value()));
        }
    }
}
