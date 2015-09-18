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
package com.forsuredb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.forsuredb.api.FSFilter;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSRecordResolver;
import com.forsuredb.api.FSSaveApi;
import com.forsuredb.api.FSTableCreator;
import com.forsuredb.api.Retriever;

import java.util.List;

/**
 * <p>
 *     The backbone of forsuredbandroid. You <i>MUST</i> call {@link #init(Context, List)} or
 *     {@link #init(Context, String, List)} in your override of
 *     {@link android.app.Application#onCreate Application.onCreate()} in your application or
 *     at some place in your application before you attempt to use it.
 * </p>
 * <p>
 *     After initialization, you can access tables with the following methods:
 * </p>
 * <ul>
 *     <li>{@link #resolve(String)} to resolve tables by name</li>
 *     <li>{@link #resolve(Uri)} to resolve tables by {@link Uri}</li>
 * </ul>
 * @author Ryan Scott
 * @see #init(Context, List)
 * @see #init(Context, String, List)
 */
public class ForSure {

    public static final String DEFAULT_DB_NAME = "forsuredb_default.db";
    private static final String LOG_TAG = ForSure.class.getSimpleName();

    private static Context appContext;
    private static FSIndex index;

    /**
     * <p>
     *     Uses the default database name {@link #DEFAULT_DB_NAME}
     * </p>
     * @param context The {@link Context} of your application
     * @param tableCreators The list of {@link FSTableCreator} objects generated by either
     *                      TableGenerator.generate() or TableGenerator.generate(String)
     * @see #init(Context, String, List)
     */
    public static void init(Context context, List<FSTableCreator> tableCreators) {
        init(context, DEFAULT_DB_NAME, tableCreators);
    }

    /**
     * <p>
     *     Initializes the index {@link ForSure} uses to resolve resources and initializes the
     *     database references. Once either {@link #init(Context, String, List)} or
     *     {@link #init(Context, List)} has been called for the first time, calling either
     *     method again will have no effect.
     * </p>
     * @param context The {@link Context} of your application
     * @param dbName The specific filename you want for your database
     * @param tableCreators The list of {@link FSTableCreator} objects generated by either
     *                      TableGenerator.generate() or TableGenerator.generate(String)
     * @see #init(Context, List)
     */
    public static void init(Context context, String dbName, List<FSTableCreator> tableCreators) {
        if (isInitialized()) {
            Log.w(LOG_TAG, ForSure.class.getSimpleName() + " already initialized--not initializing again");
            return;
        }

        appContext = context.getApplicationContext();
        index = new FSIndex(tableCreators);
        FSDBHelper.init(context.getApplicationContext(), dbName, tableCreators);
    }

    /**
     * @return false if not yet initialized; true otherwise
     */
    public static boolean isInitialized() {
        return appContext != null && index != null;
    }

    /**
     * @return The {@link SQLiteDatabase} associated with {@link ForSure} that you can use for reads
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     */
    public static SQLiteDatabase getReadableDatabase() {
        throwIfUninitialized("getReadableDatabase");
        return FSDBHelper.inst().getReadableDatabase();
    }

    /**
     * @return The {@link SQLiteDatabase} associated with {@link ForSure} that you can use for writes
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     */
    public static SQLiteDatabase getWritableDatabase() {
        throwIfUninitialized("getWritableDatabase");
        return FSDBHelper.inst().getWritableDatabase();
    }

    /**
     * <p>
     *     This does not give you all records for a table, but rather, a {@link Uri} that can be used
     *     to locate all records of the table.
     * </p>
     * @param tableName The name of the table you for which you want the {@link Uri} for all records
     * @return A {@link Uri} that can be used to locate all records of a table
     * @throws UnresolvableTableException if the table does not exist in {@link ForSure}'s index
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     */
    public static Uri all(String tableName) {
        return index.getByName(tableName).getAllRecordsUri();
    }

    /**
     * <p>
     *     Performs the query for all records and all columns of a table in no specific sort order
     * </p>
     * @param tableName The table you want to query
     * @return A {@link FSCursor} that can be iterated over all records of the table
     * @throws UnresolvableTableException if the table does not exist in {@link ForSure}'s index
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     * @see #all(String)
     */
    public static FSCursor queryAll(String tableName) {
        return new FSCursor(appContext.getContentResolver().query(all(tableName), null, null, null, null));
    }

    /**
     * <p>
     *     If you have any doubts when trying to resolve a table by its name, you should call this
     *     method first. Notably, it <i>DOES NOT</i> throw an {@link UnresolvableTableException} when
     *     the table does not exist in the index. Therefore you can use this method to avoid
     *     encountering {@link RuntimeException}s due to unresolvable lookups.
     * </p>
     * @param tableName the name of the table you want to resolve.
     * @return true if the table can be resolved, false otherwise
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     * @see #canResolve(Uri)
     */
    public static boolean canResolve(String tableName) {
        throwIfUninitialized("canResolve");
        return index.exists(tableName);
    }

    /**
     * <p>
     *     If you have any doubts when trying to resolve a table by a {@link Uri}, you should
     *     call this method first. Notably, it <i>DOES NOT</i> throw an
     *     {@link UnresolvableTableException} when the table cannot be resolved. Therefore
     *     you can use this method to avoid encountering {@link RuntimeException}s due to
     *     unresolvable lookups.
     * </p>
     * @param uri the {@link Uri} you would like to resolve
     * @return true if the table can be resolved, false otherwise
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     */
    public static boolean canResolve(Uri uri) {
        throwIfUninitialized("canResolve");
        return index.getByUri(uri) != null;
    }

    /**
     *<p>
     *     Resolve a table by table name so that you can gain access to interact with it
     *</p>
     * @param tableName The name of the table you would like to resolve
     * @return an {@link AndroidRecordResolver} capable of resolving records and providing
     * access to {@link FSTableDescriber}, and {@link FSSaveApi} objects associated with the
     * table specified by the table name passed in
     * @throws UnresolvableTableException when the table cannot be resolved
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     * @see #resolve(Uri)
     */
    public static <F extends FSFilter<Uri>> AndroidRecordResolver<F> resolve(final String tableName) {
        if (!canResolve(tableName)) {
            UnresolvableTableException ute = new UnresolvableTableException(tableName);
            Log.e(LOG_TAG, "Could not resolve table: " + tableName, ute);
            throw ute;
        }
        return resolve(index.getByName(tableName).getAllRecordsUri());
    }

    /**
     * <p>
     *     Resolve a table by a {@link Uri} so that you can gain access to interact with it.
     * </p>
     * @param resource the {@link Uri} you would like to resolve
     * @return an {@link FSFilter<Uri>} capable of providing access to {@link FSTableDescriber},
     * {@link FSGetApi}, and {@link FSSaveApi} objects associated with the table
     * @throws UnresolvableTableException when the table cannot be resolved
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     * @see #resolve(String)
     */
    public static <F extends FSFilter<Uri>> AndroidRecordResolver<F> resolve(final Uri resource) {
        throwIfUninitialized("resolve");
        final FSTableDescriber table = index.getByUri(resource);
        final ContentProviderQueryable cpq = new ContentProviderQueryable(appContext, resource);
        if (table == null) {
            UnresolvableTableException ute = new UnresolvableTableException(resource);
            Log.e(LOG_TAG, "Resource could not be resolved", ute);
            throw ute;
        }

        return new AndroidRecordResolver<F>(table, cpq);
    }

    /**
     * <p>
     *     Use this method when you want to use an {@link FSGetApi} in a method chain call or
     *     without setting a temporary variable so that the exact type can be inferred.
     * </p>
     * @param getApiClass The {@link Class} object of the {@link FSGetApi} extension you would
     *                    like to get
     * @param <T> a Type extending {@link FSGetApi}
     * @return A concrete object of type T
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     * @see #resolve(String)
     * @see #resolve(Uri)
     */
    public static <T extends FSGetApi> T getApi(Class<T> getApiClass) {
        throwIfUninitialized("getApi");
        final FSTableDescriber fstd = index.getByGetApi(getApiClass);
        return fstd == null ? null : (T) fstd.get();
    }

    /**
     * <p>
     *     Use this method when you want to use an {@link FSSaveApi} in a method chain call or
     *     without setting a temporary variable so that the exact type can be inferred.
     *     Generated {@link FSSaveApi} class names will end in "Setter."
     * </p>
     * <p>
     *     For example, if your {@link FSGetApi} class name is "MyTable":
     *     <pre>
     *     {@code ForSure.setApi(MyTableSetter.class).column1("column1").save();}
     *     </pre>
     *     If you were to do this with the {@link #resolve(Uri)} or {@link #resolve(String)}
     *     method, then you would have to assign to a temporary variable in order to use the
     *     {@link FSSaveApi} instance like so:
     *     <pre>
     *     {@code MyTableSetter setter = ForSure.resolve("my_table").setter();
     *       setter.column1("column1").save();}
     *     </pre>
     * </p>
     * @param saveApiClass the {@link Class} object of the {@link FSSaveApi} extension you want
     *                     to get
     * @param <T> a Type extending {@link FSSaveApi}
     * @return a concrete object of type T
     * @throws IllegalStateException if {@link ForSure} was not yet initialized
     */
    public static <T extends FSSaveApi<Uri>> T setApi(Class<T> saveApiClass) {
        throwIfUninitialized("setApi");
        final FSTableDescriber fstd = index.getBySaveApi(saveApiClass);
        return fstd == null ? null : (T) fstd.set(new ContentProviderQueryable(appContext, index.uriBySaveApiClass(saveApiClass)));
    }

    private static void throwIfUninitialized(String methodName) {
        if (!isInitialized()) {
            throw new IllegalStateException("Must call ForSure.init method prior to calling ForSure." + methodName);
        }
    }

    /**
     * <p>
     *     Thrown when a table cannot be resolved
     * </p>
     * @author Ryan Scott
     */
    public static final class UnresolvableTableException extends RuntimeException {

        /*package*/ UnresolvableTableException(String tableName) {
            super("Table named " + tableName + " was not found in the table index");
        }

        /*package*/ UnresolvableTableException(Uri resource) {
            super("Table could not be found for resource: " + resource == null ? "null" : resource.toString());
        }
    }
}
