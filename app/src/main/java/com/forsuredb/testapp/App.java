package com.forsuredb.testapp;

import android.app.Application;

import com.forsuredb.ForSure;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize ForSure. You can choose to pass in a database name or not.
        ForSure.init(this, TableGenerator.generate());
    }
}
