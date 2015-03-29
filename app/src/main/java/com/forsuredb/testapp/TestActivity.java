package com.forsuredb.testapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.forsuredb.table.FSTableDescriber;
import com.forsuredb.table.ForSure;

public class TestActivity extends ActionBarActivity {

    private TestUserCursorAdapter userCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        TestDBHelper.getInstance(this).getWritableDatabase();
        userCursorAdapter = new TestUserCursorAdapter(this);
        ((ListView) findViewById(R.id.user_list_view)).setAdapter(userCursorAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        FSTableDescriber userTableDescriber = ForSure.getInstance().getTable("user");
        userCursorAdapter.changeCursor(getContentResolver().query(userTableDescriber.getAllRecordsUri(), null, null, null, null));
    }

    @Override
    public void onPause() {
        super.onPause();
        userCursorAdapter.changeCursor(null);
    }
}
