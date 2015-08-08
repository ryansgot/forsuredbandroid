package com.forsuredb;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.forsuredb.annotation.FSColumn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/*package*/ class FSSaveAdapter {

    private static final String LOG_TAG = FSSaveAdapter.class.getSimpleName();
    private static final Map<Class<? extends FSSaveApi<Uri>>, Handler> HANDLERS = new HashMap<>();

    /**
     * <p>
     *     Create an api object capable of saving a row.
     * </p>
     * @param q
     * @param api
     * @param <T>
     * @return
     */
    public static <T extends FSSaveApi<Uri>> T create(ContentProviderQueryable q, Class<T> api) {
        final Handler handler = getOrCreateFor(q, api);
        return (T) Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, handler);
    }

    /**
     * <p>
     *     Lazily create the invocation handlers for the save API.
     * </p>
     * @param q
     * @param api
     * @return
     */
    private static Handler getOrCreateFor(ContentProviderQueryable q, Class<? extends FSSaveApi<Uri>> api) {
        Handler handler = HANDLERS.get(api);
        if (handler == null) {
            handler = new Handler(q, api);
            HANDLERS.put(api, handler);
        }
        return handler;
    }

    private static class Handler implements InvocationHandler {

        private final ContentProviderQueryable cpWrapper;
        private final Map<String, Integer> columnNameToIndexMap = new HashMap<>();
        private final ContentValues cv = new ContentValues();
        private String[] columns;
        private Type[] columnTypes;

        public Handler(ContentProviderQueryable cpWrapper, Class<? extends FSSaveApi<Uri>> api) {
            this.cpWrapper = cpWrapper;
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

        private SaveResult<Uri> performSave() {
            if (!idStored()) {  // <-- if no id has been stored, then this is almost assuredly an instertion . . . not necessarily true, but baby steps
                return performInsert();
            }
            return performUpsert();
        }

        private SaveResult<Uri> performInsert() {
            try {
                final Uri insertedUri = cpWrapper.insert(cv);
                return new ResultBuilder().insertedUri(insertedUri)
                                          .rowsAffected(insertedUri == null ? 0 : 1)
                                          .build();
            } catch (Exception e) {
                return new ResultBuilder().exception(e).build();
            } finally {
                cv.clear();
            }
        }

        private SaveResult<Uri> performUpsert() {
            FSSelection selection = new Selection("_id = ?", new String[]{(String) cv.get("_id")});
            Cursor cursor = cpWrapper.query(null, selection, null);
            try {
                if (cursor == null || cursor.getCount() < 1) {
                    return performInsert();
                }
                int rowsAffected = cpWrapper.update(cv, selection);
                return new ResultBuilder().rowsAffected(rowsAffected).build();
            } catch (Exception e) {
                Log.e(LOG_TAG, "exception while upserting: " + cv.toString(), e);
                return new ResultBuilder().exception(e).build();
            } finally {
                cv.clear();
                if (cursor != null) {
                    cursor.close();
                }
            }
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

        private void setColumnsAndTypes(Class<? extends FSSaveApi<Uri>> api) {
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

    private static class ResultBuilder {
        private Uri insertedUri = null;
        private int rowsAffected = 0;
        private Exception e = null;

        public ResultBuilder insertedUri(Uri insertedUri) {
            this.insertedUri = insertedUri;
            return this;
        }

        public ResultBuilder exception(Exception e) {
            this.e = e;
            return this;
        }

        public ResultBuilder rowsAffected(int rowsAffected) {
            this.rowsAffected = rowsAffected;
            return this;
        }

        public SaveResult<Uri> build() {
            return new SaveResult<Uri>() {
                @Override
                public Exception exception() {
                    return e;
                }

                @Override
                public Uri inserted() {
                    return insertedUri;
                }

                @Override
                public int rowsAffected() {
                    return rowsAffected;
                }
            };
        }
    }
}
