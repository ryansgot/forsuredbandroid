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

import com.forsuredb.api.Retriever;
import com.forsuredb.api.SaveResult;
import com.forsuredb.testapp.adapter.TestProfileInfoCursorAdapter;
import com.forsuredb.testapp.adapter.TestUserCursorAdapter;
import com.forsuredb.testapp.model.UserTable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Random;

public class TestActivity extends ActionBarActivity {

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

        Retriever retriever = ForSure.userTable().find().byAppRatingBetween(4.5D).andInclusive(5.3D).andFinally().get();
        if (retriever.moveToFirst()) {
            do {
                logUser(ForSure.userTable().getApi(), retriever);
            } while(retriever.moveToNext());
        }
        retriever.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        userCursorAdapter.changeCursor(ForSure.userTable().get());
        profileInfoCursorAdapter.changeCursor(ForSure.profileInfoTable().get());
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
                    profileInfoCursorAdapter.changeCursor(ForSure.profileInfoTable().get());
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
                    Log.i("TestActivity", "user rows deleted: " + ForSure.userTable().find().byId(id).andFinally().set().hardDelete());
                    profileInfoCursorAdapter.changeCursor(ForSure.profileInfoTable().get());
                    userCursorAdapter.changeCursor(ForSure.userTable().get());
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
                    userCursorAdapter.changeCursor(ForSure.userTable().get());
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
                    logResult(ForSure.profileInfoTable().find().byId(id).andFinally().set().softDelete());
                    profileInfoCursorAdapter.changeCursor(ForSure.profileInfoTable().get());
                    userCursorAdapter.changeCursor(ForSure.userTable().get());
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
//        UserTableSetter setter = OldForSure.resolve("user").setter();
        logResult(ForSure.userTable().set().appRating(generator.nextDouble())
                .competitorAppRating(new BigDecimal(generator.nextFloat()))
                .globalId(generator.nextLong())
                .id(id)
                .loginCount(generator.nextInt())
                .save());
        /*
         * The new record will be upserted--in other words, if a record with the specified id already exists, it will be
         * overwritten. Otherwise, it will insert.
         */

        userCursorAdapter.changeCursor(ForSure.userTable().get());   // <-- Update user list
    }

    private void inputRandomDataForProfileInfo(long id) {
        Random generator = new Random(new Date().getTime());
        long userId = generator.nextLong();

        /*
         * This block demonstrates saving a newly created record as a method call chain
         */
        logResult(ForSure.profileInfoTable().set().id(id)
                .emailAddress("user" + userId + "@email.com")
                .binaryData(new byte[]{(byte) (generator.nextInt() & 0xFF), (byte) (generator.nextInt() & 0xFF), (byte) 0})
                .save());
        /*
         * The new record will be upserted--in other words, if a record with the specified id already exists, it will be
         * overwritten. Otherwise, it will insert.
         */

        profileInfoCursorAdapter.changeCursor(ForSure.profileInfoTable().get());    // <-- Update profile info list
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
        Log.d("TestActivity", "SaveResult<Uri>{inserted=" + result.inserted() + ", exception=" + result.exception() + ", rowsAffected=" + result.rowsAffected() + "}");
    }

    private void logUser(UserTable userTable, Retriever retriever) {
        Log.i("TestActivity", new StringBuilder("_id = ").append(userTable.id(retriever))
                .append("; created = ").append(userTable.created(retriever))
                .append("; deleted = ").append(userTable.deleted(retriever))
                .append("; modified = ").append(userTable.modified(retriever))
                .append("; global_id = ").append(userTable.globalId(retriever))
                .append("; login_count = ").append(userTable.loginCount(retriever))
                .append("; app_rating = ").append(userTable.appRating(retriever))
                .append("; competitor_app_rating = ").append(userTable.competitorAppRating(retriever))
                .toString());
    }
}
