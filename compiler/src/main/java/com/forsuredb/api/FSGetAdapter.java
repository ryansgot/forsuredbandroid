/*
   forsuredb, an object relational mapping tool

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.forsuredb.api;

import com.forsuredb.annotation.FSColumn;
import com.forsuredb.annotation.FSTable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 *     Able to create an instance of any {@link FSGetApi} extension.
 * </p>
 * @author Ryan Scott
 */
public class FSGetAdapter {

    /*package*/ static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // does not prefix the column at all
    private static final Map<Class<? extends FSGetApi>, Handler> handlerMap = new HashMap<>();

    // This prefixes each column with tableName + "_"
    private static final Map<Class<? extends FSGetApi>, Handler> unambiguousHandlerMap = new HashMap<>();

    /*package*/ static final Map<Type, Method> methodMap = new HashMap<>();
    static {
        try {
            methodMap.put(BigDecimal.class, Retriever.class.getDeclaredMethod("getString", String.class));
            methodMap.put(boolean.class, Retriever.class.getDeclaredMethod("getInt", String.class));
            methodMap.put(byte[].class, Retriever.class.getDeclaredMethod("getBlob", String.class));
            methodMap.put(double.class, Retriever.class.getDeclaredMethod("getDouble", String.class));
            methodMap.put(int.class, Retriever.class.getDeclaredMethod("getInt", String.class));
            methodMap.put(long.class, Retriever.class.getDeclaredMethod("getLong", String.class));
            methodMap.put(String.class, Retriever.class.getDeclaredMethod("getString", String.class));
            methodMap.put(Date.class, Retriever.class.getDeclaredMethod("getString", String.class));
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * <p>
     *     Creates an instance of the table api class passed in that references columns by
     *     their name within the scope of the table. If you use this with a join, beware
     *     that if the joined tables have the same name, then you may not get the result
     *     you want.
     * </p>
     * @param tableApi The Class object of the {@link FSGetApi} for which you would like an instance
     * @param <T> The {@link FSGetApi} extension's type
     * @return an instance of the {@link FSGetApi} interface class passed in
     */
    public static <T extends FSGetApi> T create(Class<T> tableApi) {
        GetApiValidator.validateClass(tableApi);
        return (T) Proxy.newProxyInstance(tableApi.getClassLoader(), new Class<?>[] {tableApi}, getHandlerFor(tableApi));
    }

    /**
     * <p>
     *     Creates an instance of the table api class passed in that references columns in a
     *     tableName_columnName format so that it can be unambiguous. This is helpful for joins
     *     because different tables may have the same column name
     * </p>
     * @param tableApi The Class object of the {@link FSGetApi} for which you would like an instance
     * @param <T> The {@link FSGetApi} extension's type
     * @return an instance of the {@link FSGetApi} interface class passed in
     */
    public static <T extends FSGetApi> T createUnambiguous(Class<T> tableApi) {
        GetApiValidator.validateClass(tableApi);
        return (T) Proxy.newProxyInstance(tableApi.getClassLoader(), new Class<?>[] {tableApi}, getUnambiguousHandlerFor(tableApi));
    }

    private static Handler getHandlerFor(Class<? extends FSGetApi> tableApi) {
        Handler h = handlerMap.get(tableApi);
        if (h == null) {
            h = new Handler(tableApi);
            handlerMap.put(tableApi, h);
        }
        return h;
    }

    private static Handler getUnambiguousHandlerFor(Class<? extends FSGetApi> tableApi) {
        Handler h = unambiguousHandlerMap.get(tableApi);
        if (h == null) {
            h = new Handler(tableApi, true);
            unambiguousHandlerMap.put(tableApi, h);
        }
        return h;
    }

    private static String getColumnName(Method m) {
        return m.isAnnotationPresent(FSColumn.class) ? m.getAnnotation(FSColumn.class).value() : m.getName();
    }

    private static class Handler implements InvocationHandler {

        private final String tableName;
        private final Map<Method, String> methodToColumnNameMap;

        public Handler(Class<? extends FSGetApi> tableApi) {
            this(tableApi, false);
        }

        public Handler(Class<? extends FSGetApi> tableApi, boolean isUnambiguous) {
            tableName = tableApi.getAnnotation(FSTable.class).value();
            methodToColumnNameMap = createMethodToColumnNameMap(tableApi, isUnambiguous);
        }

        /**
         * <p>
         *     Generates a Proxy for the FSApi interface created by the client.
         * </p>
         * @param proxy
         * @param method not actually ever called, rather, it stores the meta-data associated with a
         *               call to one of the Cursor class methods
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
            return callRetrieverMethod((Retriever) args[0], methodToColumnNameMap.get(method), method.getGenericReturnType());
        }

        private Object callRetrieverMethod(Retriever retriever, String column, Type type)
                                                                        throws InvocationTargetException, IllegalAccessException {
            // TODO: find out a better solution for translation methods
            final Method cursorMethod = methodMap.get(type);
            if (type.equals(BigDecimal.class)) {
                return getBigDecimalFrom(cursorMethod, retriever, column);
            } else if (type.equals(boolean.class)) {
                final Object o = cursorMethod.invoke(retriever, column);
                return o != null && (Integer) o == 1;
            } else if (type.equals(Date.class)) {
                return getDateFrom(cursorMethod, retriever, column);
            }
            return cursorMethod.invoke(retriever, column);
        }

        private Date getDateFrom(Method cursorMethod, Retriever retriever, String column)
                                                                        throws InvocationTargetException, IllegalAccessException {
            try {
                final Object returned = cursorMethod.invoke(retriever, column);
                return returned == null ? null : DATETIME_FORMAT.parse((String) returned);
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
            return null;
        }

        private BigDecimal getBigDecimalFrom(Method retrieverMethod, Retriever retriever, String column)
                                                                        throws InvocationTargetException, IllegalAccessException {
            try {
                final Object returned = retrieverMethod.invoke(retriever, column);
                return returned == null ? null : new BigDecimal((String) returned);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            return null;
        }

        private Map<Method, String> createMethodToColumnNameMap(Class<?> tableApi, boolean isUnambiguous) {
            Map<Method, String> ret = new HashMap<>();
            for (Method m : tableApi.getDeclaredMethods()) {
                ret.put(m, (isUnambiguous ? tableName + "_" : "") + getColumnName(m));
            }
            for (Class<?> superTableApi : tableApi.getInterfaces()) {
                for (Method m : superTableApi.getDeclaredMethods()) {
                    ret.put(m, (isUnambiguous ? tableName + "_" : "") + getColumnName(m));
                }
            }
            return ret;
        }
    }
}
