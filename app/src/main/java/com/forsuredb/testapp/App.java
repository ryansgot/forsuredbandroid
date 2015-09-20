package com.forsuredb.testapp;

import android.app.Application;

import com.forsuredb.FSDBHelper;
import com.forsuredb.ForSureAndroidInfoFactory;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FSDBHelper.init(this, "testapp.db", TableGenerator.generate());
        ForSure.init(new ForSureAndroidInfoFactory(this));
    }
}
