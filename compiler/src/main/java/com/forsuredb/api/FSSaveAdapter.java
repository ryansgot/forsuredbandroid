package com.forsuredb.api;

import com.forsuredb.annotation.FSColumn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FSSaveAdapter {

    private static final Map<Class<? extends FSSaveApi>, Handler> HANDLERS = new HashMap<>();

    /**
     * <p>
     *     Create an api object capable of saving a row.
     * </p>
     *
     * @param queryable
     * @param emptyRecord
     * @param api
     * @param <T> Some FSSaveApi
     * @param <U> The class by which the save api reports its inserted rows (In Android, for example, this is a Uri)
     * @param <R> The class by which records are created (In Android, for example, this is supported by a wrapped ContentValues)
     * @return An implementation of the api class passed in.
     */
    public static <T extends FSSaveApi<U>, U, R extends RecordContainer> T create(FSQueryable<U, R> queryable,
                                                                                  R emptyRecord,
                                                                                  Class<T> api) {
        final Handler handler = getOrCreateFor(queryable, emptyRecord, api);
        return (T) Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, handler);
    }

    // Lazily create the invocation handlers for the save API.
    private static <U, R extends RecordContainer> Handler<U, R> getOrCreateFor(FSQueryable<U, R> queryable,
                                                                               R emptyRecord,
                                                                               Class<? extends com.forsuredb.api.FSSaveApi<U>> api) {
        Handler handler = HANDLERS.get(api);
        if (handler == null) {
            handler = new Handler(queryable, emptyRecord, api);
            HANDLERS.put(api, handler);
        }
        return handler;
    }

    private static class Handler<U, R extends RecordContainer> implements InvocationHandler {

        private final FSQueryable<U, R> queryable;
        private final Map<String, Integer> columnNameToIndexMap = new HashMap<>();
        private final R recordContainer;
        private String[] columns;
        private Type[] columnTypes;

        public Handler(FSQueryable<U, R> queryable, R recordContainer, Class<? extends com.forsuredb.api.FSSaveApi<U>> api) {
            this.queryable = queryable;
            this.recordContainer = recordContainer;
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

        private SaveResult<U> performSave() {
            if (!idStored()) {  // <-- if no id has been stored, then this is almost assuredly an instertion . . . not necessarily true, but baby steps
                return performInsert();
            }
            return performUpsert();
        }

        private SaveResult<U> performInsert() {
            try {
                final U inserted = queryable.insert(recordContainer);
                return ResultFactory.create(inserted, inserted == null ? 0 : 1, null);
            } catch (Exception e) {
                return ResultFactory.create(null, 0, e);
            } finally {
                recordContainer.clear();
            }
        }

        private SaveResult<U> performUpsert() {
            FSSelection selection = new Selection("_id = ?", new String[]{(String) recordContainer.get("_id")});
            Retriever cursor = queryable.query(null, selection, null);
            try {
                if (cursor == null || cursor.getCount() < 1) {
                    return performInsert();
                }
                int rowsAffected = queryable.update(recordContainer, selection);
                return ResultFactory.create(null, rowsAffected, null);
            } catch (Exception e) {
                return ResultFactory.create(null, 0, e);
            } finally {
                recordContainer.clear();
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private void performSet(String column, Object arg) {
            Type type = columnTypes[columnNameToIndexMap.get(column).intValue()];
            if (type.equals(byte[].class)) {
                recordContainer.put(column, (byte[]) arg);
            } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                recordContainer.put(column, (Boolean) arg ? 0 : 1);
            } else {
                recordContainer.put(column, arg.toString());
            }
        }

        private void setColumnsAndTypes(Class<? extends com.forsuredb.api.FSSaveApi<U>> api) {
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
            return recordContainer.get("_id") != null;
        }
    }

    private static class Selection implements FSSelection {

        private final String where;
        private final String[] replacements;

        public Selection(String where, String[] replacements) {
            this.where = where;
            this.replacements = replacements;
        }

        @Override
        public String where() {
            return where;
        }

        @Override
        public String[] replacements() {
            return replacements;
        }
    }

    private static class ResultFactory {
        public static <U> SaveResult<U> create(final U inserted, final int rowsAffected, final Exception e) {
            return new SaveResult<U>() {
                @Override
                public Exception exception() {
                    return e;
                }

                @Override
                public U inserted() {
                    return inserted;
                }

                @Override
                public int rowsAffected() {
                    return rowsAffected;
                }
            };
        }
    }
}
