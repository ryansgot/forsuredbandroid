/*
   forsuredb, an object relational mapping tool

   Copyright 2015 Ryan Scott

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
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
