package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.FSJoin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;

import static com.fsryan.forsuredb.queryable.Assertions.assertFSJoinEquals;
import static com.fsryan.forsuredb.TestObjectUtil.createFSJoin;

@RunWith(Parameterized.class)
public class UriAnalyzerFSJoinTest {

    private final FSJoin fsJoin;
    private final String stringified;

    public UriAnalyzerFSJoinTest(FSJoin fsJoin, String stringified) {
        this.fsJoin = fsJoin;
        this.stringified = stringified;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object [][] {
                {   // 00: natural join with no columns
                        createFSJoin(FSJoin.Type.NATURAL, 0),
                        "NATURAL:child:parent"
                },
                {   // 01: cross join with one column
                        createFSJoin(FSJoin.Type.CROSS, 1),
                        "CROSS:child:parent:child_column1=parent_column1"
                },
                {   // 02: inner join with 2 columns
                        createFSJoin(FSJoin.Type.INNER, 2),
                        "INNER:child:parent:child_column1=parent_column1:child_column2=parent_column2"
                },
                {   // 03: left join with 3 columns
                        createFSJoin(FSJoin.Type.LEFT, 3),
                        "LEFT:child:parent:child_column1=parent_column1:child_column2=parent_column2:child_column3=parent_column3"
                },
                {   // 04: left outer join with 4 columns
                        createFSJoin(FSJoin.Type.LEFT_OUTER, 4),
                        "LEFT_OUTER:child:parent:child_column1=parent_column1:child_column2=parent_column2:child_column3=parent_column3:child_column4=parent_column4"
                },
                {   // 05: outer join with 5 columns
                        createFSJoin(FSJoin.Type.OUTER, 5),
                        "OUTER:child:parent:child_column1=parent_column1:child_column2=parent_column2:child_column3=parent_column3:child_column4=parent_column4:child_column5=parent_column5"
                },
                {   // 06: natural outer join with 6 columns
                        createFSJoin(FSJoin.Type.NATURAL, 6),
                        "NATURAL:child:parent:child_column1=parent_column1:child_column2=parent_column2:child_column3=parent_column3:child_column4=parent_column4:child_column5=parent_column5:child_column6=parent_column6"
                }
        });
    }

    // Order of the childToParentColumnMap being read is arbitrary, and not important, so just
    // ensure that the stringified version creates an equivalent object to the object that was used
    // to create the string
    @Test
    public void shouldCorrectlyStringifyFSJoin() throws ParseException {
        final String localStringified = UriAnalyzer.stringify(fsJoin);
        FSJoin fromLocalStringified = UriAnalyzer.destringifyFSJoin(localStringified);
        assertFSJoinEquals(fsJoin, fromLocalStringified);
    }

    @Test
    public void shouldCorrectlyDestringifyFSJoin() throws ParseException {
        final FSJoin actual = UriAnalyzer.destringifyFSJoin(stringified);
        assertFSJoinEquals(fsJoin, actual);
    }
}
