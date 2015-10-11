package com.forsuredb.testapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.forsuredb.api.FSJoin;
import com.forsuredb.api.Retriever;
import com.forsuredb.api.SaveResult;
import com.forsuredb.testapp.adapter.TestProfileInfoCursorAdapter;
import com.forsuredb.testapp.adapter.TestUserCursorAdapter;
import com.forsuredb.testapp.model.ProfileInfoTable;
import com.forsuredb.testapp.model.UserTable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

import static com.forsuredb.testapp.ForSure.*;

public class TestActivity extends ActionBarActivity {

    private static final String LOG_TAG = TestActivity.class.getSimpleName();

    private TestUserCursorAdapter userCursorAdapter;
    private TestProfileInfoCursorAdapter profileInfoCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        userCursorAdapter = new TestUserCursorAdapter(this);
        ((ListView) findViewById(R.id.user_list_view)).setAdapter(userCursorAdapter);
        profileInfoCursorAdapter = new TestProfileInfoCursorAdapter(this);
        ((ListView) findViewById(R.id.profile_info_list_view)).setAdapter(profileInfoCursorAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        userCursorAdapter.changeCursor(userTable().get());
        profileInfoCursorAdapter.changeCursor(profileInfoTable().get());

        Log.i(LOG_TAG, "userTable().tableLocator() = " + userTable().tableLocator());
        Log.i(LOG_TAG, "Example of fluent API");
        Retriever retriever = userTable().find().byAppRatingBetween(4.5D).andInclusive(5.3D).andFinally().get();
        if (retriever.moveToFirst()) {
            do {
                logUser(userTable().getApi(), retriever);
            } while(retriever.moveToNext());
        }
        retriever.close();

        Log.i(LOG_TAG, "Example of autojoin (INNER) starting from User Table");
        Retriever joinRetriever = userTable().joinProfileInfoTable(FSJoin.Type.INNER).get();
        if (joinRetriever.moveToFirst()) {
            do {
                logProfileInfoTableJoinUserTable(userTable().getApi(), profileInfoTable().getApi(), joinRetriever);
            } while(joinRetriever.moveToNext());
        }
        joinRetriever.close();

        Log.i(LOG_TAG, "Example of autojoin (INNER) starting from Profile Info Table");
        joinRetriever = profileInfoTable().joinUserTable(FSJoin.Type.INNER).get();
        if (joinRetriever.moveToFirst()) {
            do {
                logProfileInfoTableJoinUserTable(userTable().getApi(), profileInfoTable().getApi(), joinRetriever);
            } while(joinRetriever.moveToNext());
        }
        joinRetriever.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        userCursorAdapter.changeCursor(null);
        profileInfoCursorAdapter.changeCursor(null);
    }

    public void onEditUserTableClicked(View v) {
        showDialog("Randomize User", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    long id = getIdFromDialog(dialogInterface);
                    inputRandomDataForUser(id);
                    profileInfoCursorAdapter.changeCursor(profileInfoTable().get());
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
                    Log.i(LOG_TAG, "user rows deleted: " + userTable().find().byId(id).andFinally().set().hardDelete());
                    profileInfoCursorAdapter.changeCursor(profileInfoTable().get());
                    userCursorAdapter.changeCursor(userTable().get());
                } catch (NumberFormatException nfe) {
                    return;
                } finally {
                    dialogInterface.dismiss();
                }
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
                    userCursorAdapter.changeCursor(userTable().get());
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
                    logResult(profileInfoTable().find().byId(id).andFinally().set().softDelete());
                    profileInfoCursorAdapter.changeCursor(profileInfoTable().get());
                    userCursorAdapter.changeCursor(userTable().get());
                } catch (NumberFormatException nfe) {
                    return;
                } finally {
                    dialogInterface.dismiss();
                }
            }
        });
    }

    private void inputRandomDataForUser(long id) {
        Random generator = new Random(new Date().getTime());

        /*
         * This block demonstrates saving a newly created record routed via the table name
         */
        logResult(userTable().find().byId(id).andFinally().set()
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

        userCursorAdapter.changeCursor(userTable().get());   // <-- Update user list
    }

    private void inputRandomDataForProfileInfo(long id) {
        Random generator = new Random(new Date().getTime());
        long userId = generator.nextLong();

        /*
         * This block demonstrates saving a newly created record as a method call chain
         */
        logResult(profileInfoTable().find().byId(id).andFinally().set()
                .id(id)
                .emailAddress("user" + userId + "@email.com")
                .binaryData(new byte[]{(byte) (generator.nextInt() & 0xFF), (byte) (generator.nextInt() & 0xFF), (byte) 0})
                .save());
        /*
         * The new record will be upserted--in other words, if a record with the specified id already exists, it will be
         * overwritten. Otherwise, it will insert.
         */

        profileInfoCursorAdapter.changeCursor(profileInfoTable().get());    // <-- Update profile info list
    }

    private long getIdFromDialog(DialogInterface dialogInterface) throws NumberFormatException {
        EditText idText = (EditText) ((AlertDialog) dialogInterface).findViewById(R.id.id_input_text);
        return Long.parseLong(idText.getText().toString());
    }

    private void showDialog(String title, DialogInterface.OnClickListener saveListener, DialogInterface.OnClickListener deleteListener) {
        new AlertDialog.Builder(this).setTitle(title)
                .setView(LayoutInflater.from(this).inflate(R.layout.enter_id_layout, null))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Save", saveListener)
                .setNeutralButton("Delete", deleteListener)
                .create()
                .show();
    }

    private void logResult(SaveResult<Uri> result) {
        Log.d(LOG_TAG, "SaveResult<Uri>{inserted=" + result.inserted() + ", exception=" + result.exception() + ", rowsAffected=" + result.rowsAffected() + "}");
    }

    private void logUser(UserTable userTable, Retriever retriever) {
        Log.i(LOG_TAG, new StringBuilder("_id = ").append(userTable.id(retriever))
                .append("; created = ").append(userTable.created(retriever))
                .append("; deleted = ").append(userTable.deleted(retriever))
                .append("; modified = ").append(userTable.modified(retriever))
                .append("; global_id = ").append(userTable.globalId(retriever))
                .append("; login_count = ").append(userTable.loginCount(retriever))
                .append("; app_rating = ").append(userTable.appRating(retriever))
                .append("; competitor_app_rating = ").append(userTable.competitorAppRating(retriever))
                .toString());
    }

    private void logProfileInfoTableJoinUserTable(UserTable userTable, ProfileInfoTable profileInfoTable, Retriever retriever) {
        Log.i(LOG_TAG, new StringBuilder("user_table._id = ").append(userTable.id(retriever))
                .append("; user_table.created = ").append(userTable.created(retriever))
                .append("; user_table.deleted = ").append(userTable.deleted(retriever))
                .append("; user_table.modified = ").append(userTable.modified(retriever))
                .append("; user_table.global_id = ").append(userTable.globalId(retriever))
                .append("; user_table.login_count = ").append(userTable.loginCount(retriever))
                .append("; user_table.app_rating = ").append(userTable.appRating(retriever))
                .append("; user_table.competitor_app_rating = ").append(userTable.competitorAppRating(retriever))
                .append("; profile_info_table._id = ").append(profileInfoTable.id(retriever))
                .append("; profile_info_table.created = ").append(profileInfoTable.created(retriever))
                .append("; profile_info_table.deleted = ").append(profileInfoTable.deleted(retriever))
                .append("; profile_info_table.modified = ").append(profileInfoTable.modified(retriever))
                .append("; profile_info_table.user_id = ").append(profileInfoTable.userId(retriever))
                .append("; profile_info_table.email_address = ").append(profileInfoTable.emailAddress(retriever))
                .append("; profile_info_table.binary_data = ").append(profileInfoTable.binaryData(retriever))
                .toString());
    }
}
