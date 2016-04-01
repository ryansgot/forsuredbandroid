package com.forsuredb.testapp;

import android.app.Application;

import com.forsuredb.FSDBHelper;
import com.forsuredb.ForSureAndroidInfoFactory;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FSDBHelper.initDebug(this, "testapp.db", TableGenerator.generate());
        ForSureAndroidInfoFactory.init(this, "com.forsuredb.testapp.content");
        ForSure.init(ForSureAndroidInfoFactory.inst());
    }
}
