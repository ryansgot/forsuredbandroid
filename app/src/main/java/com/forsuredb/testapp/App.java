package com.forsuredb.testapp;

import android.app.Application;

// TODO(ryan): move initialization of ForSure into App, initialize with Context and authority string
// TODO(ryan): move ContentProvider, DBHelper, and ContentProviderHelper into lib

public class App extends Application {

    public void onCreate() {
        super.onCreate();
        //ForSure.init(this, "authority_string");
    }
}
