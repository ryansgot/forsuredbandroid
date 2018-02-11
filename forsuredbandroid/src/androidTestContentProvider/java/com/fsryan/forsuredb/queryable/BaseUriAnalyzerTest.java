package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import org.junit.Before;

public abstract class BaseUriAnalyzerTest {

    protected final Uri input;
    protected UriAnalyzer analyzerUnderTest;

    public BaseUriAnalyzerTest(Uri input) {
        this.input = input;
    }

    @Before
    public void initAnalyzerUnderTest() {
        analyzerUnderTest = new UriAnalyzer(input);
    }
}
