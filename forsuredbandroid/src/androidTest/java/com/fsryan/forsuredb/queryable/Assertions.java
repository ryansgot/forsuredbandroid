package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Limits;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class Assertions {

    public static void assertFSJoinEquals(FSJoin expected, FSJoin actual) {
        if (verifiedNullEquality(expected, actual)) {
            return;
        }
        assertEquals(expected.getChildTable(), actual.getChildTable());
        assertEquals(expected.getParentTable(), actual.getParentTable());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getChildToParentColumnMap(), actual.getChildToParentColumnMap());
    }

    public static void assertLimitsEquals(Limits expected, Limits actual) {
        if (verifiedNullEquality(expected, actual)) {
            return;
        }
        assertEquals(expected.count(), actual.count());
        assertEquals(expected.offset(), actual.offset());
        assertEquals(expected.isBottom(), actual.isBottom());
    }

    public static void assertFSSelectionEquals(FSSelection expected, FSSelection actual) {
        if (verifiedNullEquality(expected, actual)) {
            return;
        }
        assertEquals(expected.where(), actual.where());
        assertArrayEquals(expected.replacements(), actual.replacements());
        assertLimitsEquals(expected.limits(), actual.limits());
    }

    public static void assertFSOrderingEquals(FSOrdering expected, FSOrdering actual) {
        if (verifiedNullEquality(expected, actual)) {
            return;
        }
        assertEquals(expected.table, actual.table);
        assertEquals(expected.column, actual.column);
        assertEquals(expected.direction, actual.direction);
    }

    private static boolean verifiedNullEquality(Object expected, Object actual) {
        if (expected == null) {
            assertNull(actual);
            return true;
        }
        assertNotNull(actual);
        return false;
    }
}
