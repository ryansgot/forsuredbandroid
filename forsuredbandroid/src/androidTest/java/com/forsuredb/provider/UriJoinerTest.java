package com.forsuredb.provider;

import android.net.Uri;
import android.test.InstrumentationTestCase;

public class UriJoinerTest extends InstrumentationTestCase {

    private static final String scheme = "content";
    private static final String authority = "com.forsuredb.provider.content";
    private static final String parentDirectory = "parent";
    private static final String childDirectory = "child";
    private static final String parentId = "some_string";
    private static final String childId = "324";

    private Uri.Builder expectedUriBuilder;
    private Uri.Builder parentUriBuilder;
    private Uri.Builder childUriBuilder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        expectedUriBuilder = new Uri.Builder().scheme(scheme).authority(authority).appendPath(parentDirectory);
        parentUriBuilder = new Uri.Builder().scheme(scheme).authority(authority).appendPath(parentDirectory);
        childUriBuilder = new Uri.Builder().scheme(scheme).authority(authority).appendPath(childDirectory);
    }

    public void testShouldCombineSpecificParentSpecificChildUri() {
        assertEquals(expectedUriBuilder.appendPath(parentId).appendPath(childDirectory).appendPath(childId).build(),
                     UriJoiner.join(parentUriBuilder.appendPath(parentId).build(),
                                    childUriBuilder.appendPath(childId).build()));
    }

    public void testShouldCombineSpecificParentNonSpecificChildUri() {
        assertEquals(expectedUriBuilder.appendPath(parentId).appendPath(childDirectory).appendPath("*").build(),
                     UriJoiner.join(parentUriBuilder.appendPath(parentId).build(),
                                    childUriBuilder.build()));
    }

    public void testShouldCombineNonSpecificParentSpecificChildUri() {
        assertEquals(expectedUriBuilder.appendPath("*").appendPath(childDirectory).appendPath(childId).build(),
                     UriJoiner.join(parentUriBuilder.build(),
                                    childUriBuilder.appendPath(childId).build()));
    }

    public void testShouldCombineNonSpecificParentNonSpecificChildUri() {
        assertEquals(expectedUriBuilder.appendPath("*").appendPath(childDirectory).appendPath("*").build(),
                     UriJoiner.join(parentUriBuilder.build(),
                                    childUriBuilder.build()));
    }
}
