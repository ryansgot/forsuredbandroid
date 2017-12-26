package com.fsryan.forsuredb.util;

import android.content.ContentValues;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockContentValuesBuilder {

    private Set<String> keySet = new HashSet<>();
    private ContentValues cv = mock(ContentValues.class);

    public MockContentValuesBuilder putInt(String key, int value) {
        when(cv.getAsInteger(key)).thenReturn(value);
        addKVPair(key, value);
        return this;
    }

    public MockContentValuesBuilder putLong(String key, long value) {
        when(cv.getAsLong(key)).thenReturn(value);
        addKVPair(key, value);
        return this;
    }

    public MockContentValuesBuilder putString(String key, String value) {
        when(cv.getAsString(key)).thenReturn(value);
        addKVPair(key, value);
        return this;
    }

    public ContentValues build() {
        when(cv.keySet()).thenReturn(keySet);
        return cv;
    }

    private void addKVPair(String key, Object value) {
        when(cv.get(key)).thenReturn(value);
        when(cv.containsKey(key)).thenReturn(true);
        keySet.add(key);
    }
}
