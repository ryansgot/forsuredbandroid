package com.forsuredb;

import android.content.ContentValues;
import android.content.Context;

import com.forsuredb.annotation.FSColumn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FSSaveAdapter {

    private static final Map<Class<? extends FSSaveApi>, Handler> handlers = new HashMap<>();

    /**
     * <p>
     *     Create an api object capable of saving a row.
     * </p>
     * @param context
     * @param api
     * @param <T>
     * @return
     */
    public static <T extends FSSaveApi> T create(Context context, Class<T> api) {
        final Handler handler = getOrCreateFor(context.getApplicationContext(), api);
        return (T) Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, handler);
    }

    /**
     * <p>
     *     Lazily create the invocation handlers for the save API.
     * </p>
     * @param appContext
     * @param api
     * @return
     */
    private static Handler getOrCreateFor(Context appContext, Class<? extends FSSaveApi> api) {
        Handler handler = handlers.get(api);
        if (handler == null) {
            handler = new Handler(appContext, api);
            handlers.put(api, handler);
        }
        return handler;
    }

    private static class Handler implements InvocationHandler {

        private final Context context;
        private final Map<String, Integer> columnNameToIndexMap = new HashMap<>();
        private final ContentValues cv = new ContentValues();
        private String[] columns;
        private Type[] columnTypes;

        public Handler(Context context, Class<? extends FSSaveApi> api) {
            this.context = context;
            setColumnsAndTypes(api);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            if (!"save".equals(method.getName())) {
                performSet(method.getAnnotation(FSColumn.class).value(), args[0]);
                return proxy;
            }

            return performSave();
        }

        private SaveResult performSave() {
            if (!idStored()) {  // <-- if no id has been stored, then this is almost assuredly an instertion . . . not necessarily true, but baby steps
                return performInsert();
            }
            return performUpsert();
        }

        private SaveResult performInsert() {
            // perform insertion
            cv.clear();
            return new Result(0);
        }

        private SaveResult performUpsert() {
            // perform upsertion
            cv.clear();
            return new Result(0);
        }

        private void performSet(String column, Object arg) {
            Type type = columnTypes[columnNameToIndexMap.get(column).intValue()];
            if (type.equals(byte[].class)) {
                cv.put(column, (byte[]) arg);
            } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                cv.put(column, (Boolean) arg ? 0 : 1);
            } else {
                cv.put(column, arg.toString());
            }
        }

        private void setColumnsAndTypes(Class<? extends FSSaveApi> api) {
            final Method[] apiDeclaredMethods = api.getDeclaredMethods();
            columns = new String[apiDeclaredMethods.length];
            columnTypes = new Type[apiDeclaredMethods.length];
            for (int i = 0; i < apiDeclaredMethods.length; i++) {
                columns[i] = apiDeclaredMethods[i].getAnnotation(FSColumn.class).value();
                columnTypes[i] = apiDeclaredMethods[i].getGenericParameterTypes()[0];
                columnNameToIndexMap.put(columns[i], i);
            }
        }

        private boolean idStored() {
            return cv.get("_id") != null;
        }
    }

    private static class Result implements SaveResult {

        private final int rowsAffected;
        private final List<Error> errors;

        public Result(List<Error> errors) {
            this(0, errors);
        }

        public Result(int rowsAffected) {
            this(rowsAffected, null);
        }

        public Result(int rowsAffected, List<Error> errors) {
            this.rowsAffected = rowsAffected;
            this.errors = errors == null ? Collections.EMPTY_LIST : errors;
        }

        @Override
        public List<Error> errors() {
            return errors;
        }

        @Override
        public int rowsAffected() {
            return rowsAffected;
        }
    }
}
