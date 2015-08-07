package com.forsuredb.testapp;

import android.app.Application;

import com.forsuredb.ForSure;
import com.forsuredb.FSTableCreator;
import com.forsuredb.testapp.contentprovider.TestContentProvider;
import com.forsuredb.testapp.model.ProfileInfoTableApi;
import com.forsuredb.testapp.model.UserTableApi;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize ForSure
        ForSure.init(this, "test.db", 1, createTableCreators());
    }

    private List<FSTableCreator> createTableCreators() {
        List<FSTableCreator> retList = new ArrayList<FSTableCreator>();
        retList.add(new FSTableCreator(TestContentProvider.AUTHORITY, UserTableApi.class, R.xml.user, "user"));
        retList.add(new FSTableCreator(TestContentProvider.AUTHORITY, ProfileInfoTableApi.class, R.xml.profile_info, "profile_info"));
        return retList;
    }
}
