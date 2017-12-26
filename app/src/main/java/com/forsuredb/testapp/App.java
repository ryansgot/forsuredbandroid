package com.forsuredb.testapp;

import android.app.Application;

import com.fsryan.forsuredb.FSDBHelper;
import com.fsryan.forsuredb.ForSureAndroidInfoFactory;

public class App extends Application {

    private static final String authority = "com.forsuredb.testapp.content";

    @Override
    public void onCreate() {
        super.onCreate();

        FSDBHelper.initDebug(this, "testapp.db", TableGenerator.generate());
        ForSure.init(ForSureAndroidInfoFactory.inst());

        // The current test app is a poor demonstration (because it relies upon content observers),
        // but the below is how you would initialize forsuredb without using a ContentProvider
//        FSDBHelper.initDebug(this, "testapp.db", TableGenerator.generate());
//        ForSureAndroidInfoFactory.initWithoutContentProvider(this);
//        ForSure.init(ForSureAndroidInfoFactory.inst());
    }
}
