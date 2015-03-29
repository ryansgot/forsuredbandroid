package com.forsuredb.testapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.forsuredb.table.FSTableDescriber;
import com.forsuredb.table.TableTracker;


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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        FSTableDescriber userTableDescriber = TableTracker.getInstance().get("user");
        userCursorAdapter.changeCursor(getContentResolver().query(userTableDescriber.getAllRecordsUri(), null, null, null, null));
    }

    @Override
    public void onPause() {
        super.onPause();
        userCursorAdapter.changeCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
