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
package com.forsuredb.api;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

public class TypedRecordContainerTest {

    private TypedRecordContainer trc;

    @Before
    public void setUp() {
        trc = new TypedRecordContainer();
    }

    @Test
    public void shouldGetCorrectLong() {
        trc.put("col", Long.MAX_VALUE);
        assertEquals(long.class, trc.getType("col"));

        long retrieved = trc.typedGet("col");
        assertEquals(Long.MAX_VALUE, retrieved);
    }

    @Test
    public void shouldGetCorrectDouble() {
        trc.put("col", Double.MAX_VALUE);
        assertEquals(double.class, trc.getType("col"));

        double retrieved = trc.typedGet("col");
        assertEquals(Double.MAX_VALUE, retrieved);
    }

    @Test
    public void shouldGetCorrectInt() {
        trc.put("col", Integer.MAX_VALUE);
        assertEquals(int.class, trc.getType("col"));

        int retrieved = trc.typedGet("col");
        assertEquals(Integer.MAX_VALUE, retrieved);
    }

    @Test
    public void shouldGetCorrectString() {
        trc.put("col", "test");
        assertEquals(String.class, trc.getType("col"));

        String retrieved = trc.typedGet("col");
        assertEquals("test", retrieved);
    }

    @Test
    public void shouldGetCorrectByteArray() {
        byte[] input = new byte[] {(byte) 4, (byte) 2};
        trc.put("col", input);
        assertEquals(byte[].class, trc.getType("col"));

        byte[] retrieved = trc.typedGet("col");
        assertEquals(input, retrieved);
    }

    @Test
    public void shouldGetNullWhenColumnDoesNotExist() {
        assertNull(trc.typedGet("col"));
    }
}
