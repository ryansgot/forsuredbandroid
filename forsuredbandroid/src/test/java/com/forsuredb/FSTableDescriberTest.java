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
package com.forsuredb;

import android.net.Uri;

import com.forsuredb.annotation.FSTable;
import com.forsuredb.api.FSGetApi;
import com.forsuredb.api.FSSaveApi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class FSTableDescriberTest {

    @FSTable("mock_get_api")
    private interface MockGetApi extends FSGetApi {}
    private interface MockGetApiSetter extends FSSaveApi<Uri> {}    // <-- The name must be the FSGetApi's name plus Setter

    private FSTableDescriber fstdUnderTest;

    @Before
    public void setUp() {
        fstdUnderTest = new FSTableDescriber("com.example.authority.content", MockGetApi.class);
    }

    @Test
    public void shouldReturnCorrectTableName() {
        assertEquals("mock_get_api", fstdUnderTest.getName());
    }

    @Test
    public void shouldReturnCorrectMimeType() {
        assertEquals("vnd.android.cursor/mock_get_api", fstdUnderTest.getMimeType());
    }

    @Test
    public void shouldGetCorrectGetApiClass() {
        assertEquals(MockGetApi.class, fstdUnderTest.getGetApiClass());
    }

    @Test
    public void shouldGetCorrectSaveApiClass() {
        assertEquals(MockGetApiSetter.class, fstdUnderTest.getSaveApiClass());
    }

    @Test
    public void shouldReturnCorrectGetApi() {
        assertTrue(fstdUnderTest.get() instanceof MockGetApi);
    }

    @Test
    public void shouldReturnCorrectSaveApi() {
        assertTrue(fstdUnderTest.set(Mockito.mock(ContentProviderQueryable.class)) instanceof MockGetApiSetter);
    }
}
