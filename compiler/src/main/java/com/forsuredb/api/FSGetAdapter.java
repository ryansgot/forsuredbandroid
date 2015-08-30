package com.forsuredb.api;

import com.forsuredb.annotation.FSColumn;

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

public class FSGetAdapter {

    /*package*/ static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

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
            } else if (type.equals(Date.class)) {
                return getDateFrom(cursorMethod, retriever, fsColumn.value());
            }
            return cursorMethod.invoke(retriever, fsColumn.value());
        }

        private Date getDateFrom(Method cursorMethod, Retriever retriever, String column)
                                                                        throws InvocationTargetException, IllegalAccessException {
            try {
                return DATETIME_FORMAT.parse((String) cursorMethod.invoke(retriever, column));
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
            return null;
        }

        private BigDecimal getBigDecimalFrom(Method retrieverMethod, Retriever retriever, String column)
                                                                        throws InvocationTargetException, IllegalAccessException {
            try {
                return new BigDecimal((String) retrieverMethod.invoke(retriever, column));
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            return null;
        }
    }
}
