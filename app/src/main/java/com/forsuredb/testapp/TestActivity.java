package com.forsuredb.testapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.forsuredb.FSTableDescriber;
import com.forsuredb.ForSure;
import com.forsuredb.SaveResult;
import com.forsuredb.testapp.adapter.TestProfileInfoCursorAdapter;
import com.forsuredb.testapp.adapter.TestUserCursorAdapter;
import com.forsuredb.testapp.model.UserTableSetter;

import java.math.BigDecimal;

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

        UserTableSetter utSetter = ForSure.inst().setApi(UserTableSetter.class);
        SaveResult res = utSetter.appRating(4.5D)
                                 .competitorAppRating(new BigDecimal("4.55"))
                                 .id(1L)
                                 .globalId(2L)
                                 .loginCount(42)
                                 .save();
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
}
