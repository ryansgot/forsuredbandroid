package com.forsuredb.record;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.google.common.collect.ImmutableMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
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
        ApiValidator.validateClass(tableApi);
        return (T) Proxy.newProxyInstance(tableApi.getClassLoader(), new Class<?>[] {tableApi}, handler);
    }

    private static class Handler implements InvocationHandler {

        /**
         * <p>
         *     Generates a Proxy for the FSApi interface created by the client.
         * </p>
         * @param proxy not actually ever used.
         * @param method not actually ever called, rather, it stores the meta-data associated with a call to one of the Cursor class methods
         * @param args The Cursor object on which one of the get methods will be called
         *
         * @return
         * @throws Throwable
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            ApiValidator.validateCall(method, args);
            return callCursorMethod((CursorWrapper) args[0], method.getAnnotation(FSColumn.class), method.getGenericReturnType());
        }

        private Object callCursorMethod(CursorWrapper cursor, FSColumn fsColumn, Type type)
                                                                        throws InvocationTargetException, IllegalAccessException {
            return cursorMethodMap.get(type).invoke(cursor, cursor.getColumnIndex(fsColumn.value()));
        }
    }
}
