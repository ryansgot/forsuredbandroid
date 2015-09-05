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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FSSaveAdapter {

    private static final Map<Class<? extends FSSaveApi>, Map<String, Type>> API_TO_COLUMNS_MAP = new HashMap<>();

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
        return (T) Proxy.newProxyInstance(api.getClassLoader(),
                                          new Class<?>[]{api},
                                          new Handler(queryable, emptyRecord, getColumnTypeMapFor(api)));
    }

    // lazily create the column type maps for each api so that they are not created each time a new handler is created
    private static <T extends FSSaveApi> Map<String, Type> getColumnTypeMapFor(Class<T> api) {
        Map<String, Type> retMap = API_TO_COLUMNS_MAP.get(api);
        if (retMap == null) {
            retMap = createColumnTypeMapFor(api);
            API_TO_COLUMNS_MAP.put(api, retMap);
        }
        return retMap;
    }

    private static <T extends FSSaveApi> Map<String, Type> createColumnTypeMapFor(Class<T> api) {
        Map<String, Type> retMap = new HashMap<>();
        for (Method m : api.getDeclaredMethods()) {
            retMap.put(getColumnName(m), m.getGenericParameterTypes()[0]);
        }
        return retMap;
    }

    private static String getColumnName(Method m) {
        return m.isAnnotationPresent(FSColumn.class) ? m.getAnnotation(FSColumn.class).value() : m.getName();
    }

    private static class Handler<U, R extends RecordContainer> implements InvocationHandler {

        private final FSQueryable<U, R> queryable;
        private final R recordContainer;
        private final Map<String, Type> columnTypeMap;

        public Handler(FSQueryable<U, R> queryable, R recordContainer, Map<String, Type> columnTypeMap) {
            this.queryable = queryable;
            this.recordContainer = recordContainer;
            this.columnTypeMap = columnTypeMap;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            if (!"save".equals(method.getName())) {
                performSet(getColumnName(method), args[0]);
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
            Type type = columnTypeMap.get(column);
            if (type.equals(byte[].class)) {
                recordContainer.put(column, (byte[]) arg);
            } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                recordContainer.put(column, (Boolean) arg ? 0 : 1);
            } else if (type.equals(Date.class)) {
                recordContainer.put(column, FSGetAdapter.DATETIME_FORMAT.format((Date) arg));
            } else {
                recordContainer.put(column, arg.toString());
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
