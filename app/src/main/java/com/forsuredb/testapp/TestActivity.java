package com.forsuredb.testapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.fsryan.forsuredb.api.FSGetApi;
import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.Retriever;
import com.fsryan.forsuredb.api.SaveResult;
import com.fsryan.forsuredb.cursor.FSCursor;
import com.fsryan.forsuredb.cursor.FSCursorLoader;
import com.fsryan.forsuredb.cursor.FSCursorRecyclerViewAdapter;
import com.fsryan.forsuredb.cursor.FSCursorViewHolder;
import com.forsuredb.testapp.adapter.ProfileInfoTableRecyclerAdapter;
import com.forsuredb.testapp.adapter.UserTableRecyclerAdapter;
import com.forsuredb.testapp.model.AdditionalDataTable;
import com.forsuredb.testapp.model.ProfileInfoTable;
import com.forsuredb.testapp.model.UserTable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

import static com.forsuredb.testapp.ForSure.*;
import static com.fsryan.forsuredb.api.OrderBy.ORDER_DESC;
import static com.fsryan.forsuredb.api.OrderBy.ORDER_ASC;

public class TestActivity extends AppCompatActivity {

    private static final int LOADER_ID = 1934;

    private static final String LOG_TAG = TestActivity.class.getSimpleName();

    private UserTableRecyclerAdapter userRecyclerAdapter;
    private ProfileInfoTableRecyclerAdapter profileInfoRecyclerAdapter;

    private JoinLoader joinLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        userRecyclerAdapter = new UserTableRecyclerAdapter();
        initRecycler(R.id.user_recycler_view, userRecyclerAdapter);
        profileInfoRecyclerAdapter = new ProfileInfoTableRecyclerAdapter();
        initRecycler(R.id.profile_info_recycler_view, profileInfoRecyclerAdapter);

        joinLoader = new JoinLoader();
        getLoaderManager().initLoader(LOADER_ID, null, joinLoader);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test, menu);
        menu.removeItem(R.id.action_test_relational);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_test_doc_store:
                startActivity(new Intent(this, DocStoreTestActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onEditUserTableClicked(View v) {
        showDialog("Randomize User", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    long id = getIdFromDialog(dialogInterface);
                    inputRandomDataForUser(id);
                } catch (NumberFormatException nfe) {
                    return;
                } finally {
                    dialogInterface.dismiss();
                }
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                try {
                    long id = getIdFromDialog(dialogInterface);
                    Log.i(LOG_TAG, "user rows deleted: " + userTable().find().byId(id).then().set().hardDelete());
                } catch (NumberFormatException nfe) {
                    return;
                } finally {
                    dialogInterface.dismiss();
                }
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SaveResult<Uri> result = userTable().set().save();
                logResult(result);
                // Won't show in UI because the default record will be excluded from the join
                Toast.makeText(TestActivity.this, "saved: " + result.inserted(), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }

    public void onEditProfileInfoTableClicked(View v) {
        showDialog("Profile Info", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    long id = getIdFromDialog(dialogInterface);
                    inputRandomDataForProfileInfo(id);
                } catch (NumberFormatException nfe) {
                    return;
                } finally {
                    dialogInterface.dismiss();
                }
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                try {
                    long id = getIdFromDialog(dialogInterface);
                    logResult(profileInfoTable().find().byId(id).then().set().softDelete());
                } catch (NumberFormatException nfe) {
                    return;
                } finally {
                    dialogInterface.dismiss();
                }
            }
        }, null);
    }

    private <G extends FSGetApi, VH extends FSCursorViewHolder> void initRecycler(int viewId, FSCursorRecyclerViewAdapter<G, VH> adapter) {
        RecyclerView recycler = (RecyclerView) findViewById(viewId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.scrollToPosition(0);
        recycler.setLayoutManager(layoutManager);
        recycler.setHasFixedSize(true);
        recycler.setAdapter(adapter);
    }

    private void inputRandomDataForUser(long id) {
        Random generator = new Random(new Date().getTime());

        /*
         * This block demonstrates saving a newly created record routed via the table name
         */
        logResult(userTable().find().byId(id).then().set()
                .appRating(generator.nextDouble())
                .competitorAppRating(new BigDecimal(generator.nextFloat()))
                .globalId(generator.nextLong())
                .id(id)
                .loginCount(generator.nextInt())
                .save());
        /*
         * The new record will be upserted--in other words, if a record with the specified id already exists, it will be
         * overwritten. Otherwise, it will insert.
         */
    }

    private void inputRandomDataForProfileInfo(long id) {
        Random generator = new Random(new Date().getTime());
        long userId = generator.nextLong();

        /*
         * This block demonstrates saving a newly created record as a method call chain
         */
        logResult(profileInfoTable().find().byId(id).then().set()
                .id(id)
                .emailAddress("user" + userId + "@email.com")
                .binaryData(new byte[]{(byte) (generator.nextInt() & 0xFF), (byte) (generator.nextInt() & 0xFF), (byte) 0})
                .awesome(generator.nextBoolean())
                .save());
        /*
         * The new record will be upserted--in other words, if a record with the specified id already exists, it will be
         * overwritten. Otherwise, it will insert.
         */
    }

    private long getIdFromDialog(DialogInterface dialogInterface) throws NumberFormatException {
        EditText idText = (EditText) ((AlertDialog) dialogInterface).findViewById(R.id.id_input_text);
        return Long.parseLong(idText.getText().toString());
    }

    private void showDialog(String title, DialogInterface.OnClickListener saveListener, DialogInterface.OnClickListener deleteListener, DialogInterface.OnClickListener defaultRecordListener) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this).setTitle(title)
                .setView(LayoutInflater.from(this).inflate(R.layout.enter_id_layout, null))
                .setNegativeButton("Delete", deleteListener)
                .setPositiveButton("Save", saveListener);
        if (defaultRecordListener != null) {
            adb.setNeutralButton("New Default Record", defaultRecordListener);
        } else {
            adb.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        adb.create().show();
    }

    private void logResult(SaveResult<Uri> result) {
        Log.d(LOG_TAG, "SaveResult<Uri>{inserted=" + result.inserted() + ", exception=" + result.exception() + ", rowsAffected=" + result.rowsAffected() + "}");
    }

    private void logProfileInfoTableJoinUserTableJoinAdditionalDataTable(UserTable userTable, ProfileInfoTable profileInfoTable, AdditionalDataTable additionalDataTable, Retriever retriever) {
        StringBuilder sb = new StringBuilder();
        if (userTable != null) {
            sb.append("User Table:\n")
                    .append("user_table._id = ").append(userTable.id(retriever))
                    .append("; user_table.created = ").append(userTable.created(retriever))
                    .append("; user_table.deleted = ").append(userTable.deleted(retriever))
                    .append("; user_table.modified = ").append(userTable.modified(retriever))
                    .append("; user_table.global_id = ").append(userTable.globalId(retriever))
                    .append("; user_table.login_count = ").append(userTable.loginCount(retriever))
                    .append("; user_table.app_rating = ").append(userTable.appRating(retriever))
                    .append("; user_table.competitor_app_rating = ").append(userTable.competitorAppRating(retriever));
        }
        if (profileInfoTable != null) {
            sb.append(sb.length() == 0 ? "" : "\n")
                    .append("Profile Info Table:\n")
                    .append("profile_info_table._id = ").append(profileInfoTable.id(retriever))
                    .append("; profile_info_table.created = ").append(profileInfoTable.created(retriever))
                    .append("; profile_info_table.deleted = ").append(profileInfoTable.deleted(retriever))
                    .append("; profile_info_table.modified = ").append(profileInfoTable.modified(retriever))
                    .append("; profile_info_table.user_id = ").append(profileInfoTable.userId(retriever))
                    .append("; profile_info_table.email_address = ").append(profileInfoTable.emailAddress(retriever))
                    .append("; profile_info_table.binary_data = ").append(profileInfoTable.binaryData(retriever))
                    .append("; profile_info_table.awesome = ").append(profileInfoTable.awesome(retriever));
        }
        if (additionalDataTable != null) {
            sb.append(sb.length() == 0 ? "" : "\n")
                    .append("Additional Data Table:\n")
                    .append("additional_data_table._id = ").append(additionalDataTable.id(retriever))
                    .append("; additional_data_table.deleted = ").append(additionalDataTable.deleted(retriever))
                    .append("; additional_data_table.modified = ").append(additionalDataTable.modified(retriever))
                    .append("; additional_data_table.created = ").append(additionalDataTable.created(retriever))
                    .append("; additional_data_table.int_column = ").append(additionalDataTable.intColumn(retriever))
                    .append("; additional_data_table.long_column = ").append(additionalDataTable.longColumn(retriever))
                    .append("; additional_data_table.string_column = ").append(additionalDataTable.stringColumn(retriever))
                    .append("; additional_data_table.profile_info_id = ").append(additionalDataTable.profileInfoId(retriever));
        }
        Log.i(LOG_TAG, sb.toString());
    }

    private class JoinLoader implements LoaderManager.LoaderCallbacks<FSCursor> {

        private final ProfileInfoTable profileInfoTable = profileInfoTable().getApi();
        private final AdditionalDataTable additionalDataTable = additionalDataTable().getApi();
        private final UserTable userTable = userTable().getApi();

        @Override
        public Loader<FSCursor> onCreateLoader(int id, Bundle args) {
            Log.i(LOG_TAG, "JoinLoader.onCreateLoader");
            /*
             * Demonstrates an incredibly flexible querying API
             * The comments to the right tell what is happening to the type system in terms of the querying context.
             *
             * Entry to and exit from the various querying contexts is marked by indentation for demonstration purposes. Because
             * this is a long chain of methods, and the generated API is specific to the queries you may want to make, the return
             * type of each method call may not be the same (and it often is not).
             *
             * There is one big gotcha with FSCursorLoader and the new querying API. The new querying API allows you to stop the
             * method call chain with a Resolver that is not the outermost resolver (in this case, the Resolver that was returned
             * from the profileInfoTable() call). In order to refresh the data on change to any table in your query, you'll have
             * to pass a reference to this outermost resolver to the constructor of the FSCursorLoader by chaining calls to then()
             * until it is no longer available to be called.
             */
            return new FSCursorLoader<>(TestActivity.this, profileInfoTable()   // <-- signifies the base table on which you will
                                                                                // SELECT and enters the profile_info table's
                                                                                // Resolver context
                    .joinAdditionalDataTable(FSJoin.Type.INNER)                 // <-- performs a JOIN to the additional_data
                                                                                // table based upon the @ForeignKey annotaiton on
                                                                                // a method in either the ProfileInfoTable
                                                                                // interface or the AdditionalDataTable interface
                                                                                // and enters the additional_data table's Resolver
                                                                                // context
                            .order()                                            // <-- enters the additional_data table's OrderBy
                                                                                // context
                                    .byId(ORDER_ASC)                            // <-- adds additional_data._id ASC to the
                                                                                // ORDER BY clause of the generated query
                                    .then()                                     // <-- exits the additional_data table's OrderBy
                                                                                // context
                            .find()                                             // <-- enters the additional_data table's Finder
                                                                                // context
                                    .byIdGreaterThan(10L)                       // <-- adds additional_data._id > 10 to the WHERE
                                                                                // clause of the generated query
                                    .then()                                     // <-- exits the additional_data table's Finder
                                                                                // context
                            .then()                                             // <-- exits the additional_data table's Resolver
                                                                                // context, entering the profile_info table's
                                                                                // Resolver context
                    .joinUserTable(FSJoin.Type.INNER)                           // <-- performs a JOIN to the user table based
                                                                                // upon the @ForeignKey annotation on a method in
                                                                                // either the ProfileInfoTable interface or the
                                                                                // UserTable interface and enters the user table's
                                                                                // Resolver context
                            .order()                                            // <-- enters the user table's OrderBy context
                                    .byLoginCount(ORDER_DESC)                   // <-- adds user.login_count DESC to the generated
                                                                                // query's ORDER BY clause
                                    .then()                                     // <-- exits the user table's OrderBy context
                            .find()                                             // <-- enters the user table's Finder context
                                    .byLoginCountGreaterThan(2)                 // <-- adds AND login_count > 2 to the generated
                                                                                // query's WHERE clause
                                    .then()                                     // <-- exits the user table's Finder context
                            .then()                                             // <-- exits the user table's Resolver context,
                                                                                // returning to the profile_info table's Resolver
                                                                                // context
                    .find()                                                     // <-- enters the profile_info table's Finder
                                                                                // context
                            .byAwesome()                                        // <-- adds AND profile_info.awesome = 1 to the
                                                                                // generated query's WHERE clause
                            .then()                                             // <-- exits the profile_info table's Finder
                                                                                // context
                    .order()                                                    // <-- enters the profile_info table's OrderBy
                                                                                // context
                            .byEmailAddress(ORDER_ASC)                          // <-- adds profile_info.email_address ASC to the
                                                                                // generated query's ORDER BY clause
                            .then());                                           // <-- exits the profile_info table's OrderBy
                                                                                // context, returning to the original Resolver
                                                                                // context of the profile_info table, as desired
                                                                                // by the constructor of FSCursorLoader
        }

        @Override
        public void onLoadFinished(Loader<FSCursor> loader, FSCursor data) {
            Log.i(LOG_TAG, "JoinLoader.onLoadFinished");
            if (data != null && data.moveToFirst()) {
                do {
                    logProfileInfoTableJoinUserTableJoinAdditionalDataTable(userTable, profileInfoTable, additionalDataTable, data);
                } while (data.moveToNext());
                profileInfoRecyclerAdapter.changeCursor(data);
                userRecyclerAdapter.changeCursor(data);
            } else {
                Log.i(LOG_TAG, "data was null or empty");
            }
        }

        @Override
        public void onLoaderReset(Loader<FSCursor> loader) {
            Log.i(LOG_TAG, "JoinLoader.onLoaderReset");
        }
    }
}
