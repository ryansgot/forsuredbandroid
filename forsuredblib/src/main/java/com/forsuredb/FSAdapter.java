package com.forsuredb;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

import com.forsuredb.record.FSColumn;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;

public class FSAdapter {

    private static final String LOG_TAG = FSAdapter.class.getSimpleName();

    /*package*/ static ImmutableMap<Type, Method> cursorMethodMap;
    static {
        try {
            cursorMethodMap = new ImmutableMap.Builder<Type, Method>().put(BigDecimal.class, Cursor.class.getDeclaredMethod("getString", int.class))
                                                                      .put(boolean.class, Cursor.class.getDeclaredMethod("getInt", int.class))
                                                                      .put(byte[].class, Cursor.class.getDeclaredMethod("getBlob", int.class))
                                                                      .put(double.class, Cursor.class.getDeclaredMethod("getDouble", int.class))
                                                                      .put(int.class, Cursor.class.getDeclaredMethod("getInt", int.class))
                                                                      .put(long.class, Cursor.class.getDeclaredMethod("getLong", int.class))
                                                                      .put(String.class, Cursor.class.getDeclaredMethod("getString", int.class))
                                                                      .build();
        } catch (NoSuchMethodException nsme) {
            Log.e(LOG_TAG, "error creating cursorMethodMap", nsme);
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
            // TODO: find out a better solution for translation methods
            final Method cursorMethod = cursorMethodMap.get(type);
            if (type.equals(BigDecimal.class)) {
                return getBigDecimalFrom(cursorMethod, cursor, cursor.getColumnIndex(fsColumn.value()));
            } else if (type.equals(boolean.class)) {
                final Object o = cursorMethod.invoke(cursor, cursor.getColumnIndex(fsColumn.value()));
                return o != null && (Integer) o == 1;
            }
            return cursorMethod.invoke(cursor, cursor.getColumnIndex(fsColumn.value()));
        }

        private BigDecimal getBigDecimalFrom(Method cursorMethod, CursorWrapper cursor, int columnIndex)
                                                                        throws InvocationTargetException, IllegalAccessException {
            try {
                return new BigDecimal((String) cursorMethod.invoke(cursor, columnIndex));
            } catch (NumberFormatException nfe) {
                Log.e(LOG_TAG, "number format exception when getting a BigDecimal from cursor at column: " + columnIndex, nfe);
            }
            return null;
        }
    }
}
