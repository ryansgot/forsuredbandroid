package com.forsuredb;

import android.util.Log;

import com.forsuredb.annotation.FSColumn;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;

/*package*/ class FSGetAdapter {

    private static final String LOG_TAG = FSGetAdapter.class.getSimpleName();

    /*package*/ static ImmutableMap<Type, Method> methodMap;
    static {
        try {
            methodMap = new ImmutableMap.Builder<Type, Method>().put(BigDecimal.class, Retriever.class.getDeclaredMethod("getString", String.class))
                                                                      .put(boolean.class, Retriever.class.getDeclaredMethod("getInt", String.class))
                                                                      .put(byte[].class, Retriever.class.getDeclaredMethod("getBlob", String.class))
                                                                      .put(double.class, Retriever.class.getDeclaredMethod("getDouble", String.class))
                                                                      .put(int.class, Retriever.class.getDeclaredMethod("getInt", String.class))
                                                                      .put(long.class, Retriever.class.getDeclaredMethod("getLong", String.class))
                                                                      .put(String.class, Retriever.class.getDeclaredMethod("getString", String.class))
                                                                      .build();
        } catch (NoSuchMethodException nsme) {
            Log.e(LOG_TAG, "error creating methodMap", nsme);
        }
    }

    private static final Handler handler = new Handler();   // <-- there only needs to be one handler ever

    public static <T> T create(Class<T> tableApi) {
        GetApiValidator.validateClass(tableApi);
        return (T) Proxy.newProxyInstance(tableApi.getClassLoader(), new Class<?>[] {tableApi}, handler);
    }

    private static class Handler implements InvocationHandler {

        /**
         * <p>
         *     Generates a Proxy for the FSApi interface created by the client.
         * </p>
         * @param proxy not actually ever used.
         * @param method not actually ever called, rather, it stores the meta-data associated with a call to one of the Cursor class methods
         * @param args The Retriever object on which one of the get methods will be called
         *
         * @return
         * @throws Throwable
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            GetApiValidator.validateCall(method, args);
            return callRetrieverMethod((Retriever) args[0], method.getAnnotation(FSColumn.class), method.getGenericReturnType());
        }

        private Object callRetrieverMethod(Retriever retriever, FSColumn fsColumn, Type type)
                                                                        throws InvocationTargetException, IllegalAccessException {
            // TODO: find out a better solution for translation methods
            final Method cursorMethod = methodMap.get(type);
            if (type.equals(BigDecimal.class)) {
                return getBigDecimalFrom(cursorMethod, retriever, fsColumn.value());
            } else if (type.equals(boolean.class)) {
                final Object o = cursorMethod.invoke(retriever, fsColumn.value());
                return o != null && (Integer) o == 1;
            }
            return cursorMethod.invoke(retriever, fsColumn.value());
        }

        private BigDecimal getBigDecimalFrom(Method retrieverMethod, Retriever retriever, String column)
                                                                        throws InvocationTargetException, IllegalAccessException {
            try {
                return new BigDecimal((String) retrieverMethod.invoke(retriever, column));
            } catch (NumberFormatException nfe) {
                Log.e(LOG_TAG, "number format exception when getting a BigDecimal from retriever at column: " + column, nfe);
            }
            return null;
        }
    }
}
