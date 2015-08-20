package com.forsuredb.testapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.forsuredb.FSTableDescriber;
import com.forsuredb.ForSure;
import com.forsuredb.testapp.adapter.TestProfileInfoCursorAdapter;
import com.forsuredb.testapp.adapter.TestUserCursorAdapter;
import com.forsuredb.testapp.model.ProfileInfoTableSetter;
import com.forsuredb.testapp.model.UserTableSetter;

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
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update user list
        FSTableDescriber userTable = ForSure.inst().getTable("user");
        userCursorAdapter.changeCursor(getContentResolver().query(userTable.getAllRecordsUri(), null, null, null, null));
        // Update profile info list
        FSTableDescriber profileTable = ForSure.inst().getTable("profile_info");
        profileInfoCursorAdapter.changeCursor(getContentResolver().query(profileTable.getAllRecordsUri(), null, null, null, null));
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
                } catch (NumberFormatException nfe) {
                    return;
                } finally {
                    dialogInterface.dismiss();
                }
            }
        });
    }

    public void onEditProfileInfoTableClicked(View v) {
        showDialog("Randomize Profile Info", new DialogInterface.OnClickListener() {
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
        });
    }

    private void inputRandomDataForUser(long id) {
        Random generator = new Random(new Date().getTime());

        /*
         * This block demonstrates saving a newly created record routed via the Uri.
         */
        ForSure.inst().setApi(UserTableSetter.class).appRating(generator.nextDouble())
                .competitorAppRating(new BigDecimal(generator.nextFloat()))
                .globalId(generator.nextLong())
                .id(id)
                .loginCount(generator.nextInt())
                .save();
        /*
         * The new record will be upserted--in other words, if a record with the specified id already exists, it will be
         * overwritten. Otherwise, it will insert.
         */

        FSTableDescriber userTable = ForSure.inst().getTable("user");
        userCursorAdapter.changeCursor(getContentResolver().query(userTable.getAllRecordsUri(), null, null, null, null));
    }

    private void inputRandomDataForProfileInfo(long id) {
        Random generator = new Random(new Date().getTime());
        long userId = generator.nextLong();

        /*
         * This block demonstrates saving a newly created record routed via the Uri.
         */
        ForSure.inst().setApi(ProfileInfoTableSetter.class).id(id)
                .emailAddress("user" + userId + "@email.com")
                .userId(userId)
                .binaryData(new byte[] {(byte) (generator.nextInt() & 0xFF), (byte) (generator.nextInt() & 0xFF), (byte) 0})
                .save();
        /*
         * The new record will be upserted--in other words, if a record with the specified id already exists, it will be
         * overwritten. Otherwise, it will insert.
         */

        FSTableDescriber profileTable = ForSure.inst().getTable("profile_info");
        profileInfoCursorAdapter.changeCursor(getContentResolver().query(profileTable.getAllRecordsUri(), null, null, null, null));
    }

    private long getIdFromDialog(DialogInterface dialogInterface) throws NumberFormatException{
        EditText idText = (EditText) ((AlertDialog) dialogInterface).findViewById(R.id.id_input_text);
        return Long.parseLong(idText.getText().toString());
    }

    private void showDialog(String title, DialogInterface.OnClickListener positiveButtonClickListener) {
        new AlertDialog.Builder(this).setTitle(title)
                .setView(LayoutInflater.from(this).inflate(R.layout.enter_id_layout, null))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("Save", positiveButtonClickListener)
                .create()
                .show();
    }
}
