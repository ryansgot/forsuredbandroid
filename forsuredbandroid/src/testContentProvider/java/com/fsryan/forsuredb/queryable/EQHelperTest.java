package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.FSJoin;
import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.OrderBy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.fsryan.forsuredb.api.OrderBy.ORDER_ASC;
import static com.fsryan.forsuredb.api.OrderBy.ORDER_DESC;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class EQHelperTest {

    private static final FSJoin join = createFSJoin(FSJoin.Type.INNER);
    private static final FSOrdering ordering = createFSOrdering(ORDER_ASC);

    private final FSJoin fsj1, fsj2;
    private final FSOrdering fso1, fso2;
    private final boolean expectedEq;

    public EQHelperTest(FSJoin fsj1, FSJoin fsj2, FSOrdering fso1, FSOrdering fso2, boolean expectedEq) {
        this.fsj1 = fsj1;
        this.fsj2 = fsj2;
        this.fso1 = fso1;
        this.fso2 = fso2;
        this.expectedEq = expectedEq;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: <-- null references should be equal
                        null,
                        null,
                        null,
                        null,
                        true
                },
                {   // 01: <-- equal references should be equal
                        join,
                        join,
                        ordering,
                        ordering,
                        true
                },
                {   // 02: <-- equal objects that are different references should be equal
                        join,
                        createFSJoin(FSJoin.Type.INNER),
                        ordering,
                        createFSOrdering(ORDER_ASC),
                        true
                },
                {   // 03: unequal type, unequal ordering should produce unequal
                        join,
                        createFSJoin(FSJoin.Type.NATURAL),
                        ordering,
                        createFSOrdering(ORDER_DESC),
                        false
                },
                {   // 04: unequal type (null), unequal ordering should produce unequal
                        join,
                        createFSJoin(null),
                        ordering,
                        createFSOrdering(ORDER_DESC),
                        false
                },
                {   // 05: unequal type (null reversed order), unequal ordering should produce unequal
                        createFSJoin(null),
                        join,
                        ordering,
                        createFSOrdering(ORDER_DESC),
                        false
                },
                {   // 06: unequal parent, unequal table name should produce unequal
                        join,
                        new FSJoin(join.getType(), "different", join.getChildTable(), join.getChildToParentColumnMap()),
                        ordering,
                        new FSOrdering("different", ordering.column, ordering.direction),
                        false
                },
                {   // 07: unequal parent (null), unequal table name (null) should produce unequal
                        join,
                        new FSJoin(join.getType(), null, join.getChildTable(), join.getChildToParentColumnMap()),
                        ordering,
                        new FSOrdering(null, ordering.column, ordering.direction),
                        false
                },
                {   // 08: unequal parent (null reversed), unequal table name (null reversed) should produce unequal
                        new FSJoin(join.getType(), null, join.getChildTable(), join.getChildToParentColumnMap()),
                        join,
                        new FSOrdering(null, ordering.column, ordering.direction),
                        ordering,
                        false
                },
                {   // 09: unequal child, unequal column name should produce unequal
                        join,
                        new FSJoin(join.getType(), join.getParentTable(), "different", join.getChildToParentColumnMap()),
                        ordering,
                        new FSOrdering(ordering.table, "diferent", ordering.direction),
                        false
                },
                {   // 10: unequal child (null), unequal column name (null) should produce unequal
                        join,
                        new FSJoin(join.getType(), join.getParentTable(), null, join.getChildToParentColumnMap()),
                        ordering,
                        new FSOrdering(ordering.table, null, ordering.direction),
                        false
                },
                {   // 11: unequal child (null reversed), unequal column name (null reversed) should produce unequal
                        new FSJoin(join.getType(), join.getParentTable(), null, join.getChildToParentColumnMap()),
                        join,
                        new FSOrdering(ordering.table, null, ordering.direction),
                        ordering,
                        false
                },
                {   // 12: unequal map
                        join,
                        new FSJoin(join.getType(), join.getParentTable(), join.getChildTable(), Collections.<String, String>emptyMap()),
                        ordering,
                        null,
                        false
                },
                {   // 13: unequal map (null
                        join,
                        new FSJoin(join.getType(), join.getParentTable(), join.getChildTable(), null),
                        ordering,
                        null,
                        false
                },
                {   // 14: unequal map
                        new FSJoin(join.getType(), join.getParentTable(), join.getChildTable(), null),
                        join,
                        null,
                        ordering,
                        false
                }
        });
    }

    @Test
    public void shouldCorrectlyEvaluateEqualsFSJoins() {
        assertEquals(expectedEq, EQHelper.joinEquals(fsj1, fsj2));
    }

    @Test
    public void shouldCorrectlyEvaluateEqualsFSJOrderings() {
        assertEquals(expectedEq, EQHelper.orderingEquals(fso1, fso2));
    }

    private static FSJoin createFSJoin(FSJoin.Type type) {
        return new FSJoin(type, "parent", "child", mapOf("c1", "p1", "c2", "p2"));
    }

    private static FSOrdering createFSOrdering(int orderBy) {
        return new FSOrdering("table", "column", orderBy);
    }

    private static Map<String, String> mapOf(String... ss) {
        Map<String, String> ret = new HashMap<>(ss.length / 2);
        for (int i = 0; i < ss.length; i += 2) {
            ret.put(ss[i], ss[i + 1]);
        }
        return ret;
    }
}
