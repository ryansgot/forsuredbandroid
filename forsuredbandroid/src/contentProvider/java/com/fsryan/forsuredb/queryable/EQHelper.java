package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;

// The below should be included in forsuredblib as overrides of .equals()
abstract class EQHelper {

    // TODO: write tests for this

    static boolean joinEquals(FSJoin j1, FSJoin j2) {
        if (j1 == j2) {
            return true;
        }
        return !(j1 == null || j2 == null)
                && nullCheckCallEquals(j1.getChildTable(), j2.getChildTable())
                && nullCheckCallEquals(j1.getParentTable(), j2.getParentTable())
                && nullCheckCallEquals(j1.getType(), j2.getType())
                && nullCheckCallEquals(j1.getChildToParentColumnMap(), j2.getChildToParentColumnMap());
    }

    static boolean orderingEquals(FSOrdering o1, FSOrdering o2) {
        if (o1 == o2) {
            return true;
        }
        return !(o1 == null || o2 == null)
                && o1.direction == o2.direction
                && nullCheckCallEquals(o1.table, o2.table)
                && nullCheckCallEquals(o1.column, o2.column);
    }

    private static boolean nullCheckCallEquals(Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));   // <-- wlog, o1 == null would mean objects are unequal
    }
}
