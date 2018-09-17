package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import com.fsryan.forsuredb.api.FSJoin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.fsryan.forsuredb.queryable.Assertions.assertFSJoinEquals;
import static com.fsryan.forsuredb.TestObjectUtil.createFSJoin;
import static com.fsryan.forsuredb.TestObjectUtil.starterUri;
import static com.fsryan.forsuredb.TestObjectUtil.tableUriWithJoins;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriAnalyzerGetJoinsTest extends BaseUriAnalyzerTest {

    private final boolean expectedHasJoin;
    private final List<FSJoin> expectedJoins;

    public UriAnalyzerGetJoinsTest(Uri input, boolean expectedHasJoin, List<FSJoin> expectedJoins) {
        super(input);
        this.expectedHasJoin = expectedHasJoin;
        this.expectedJoins = expectedJoins;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: no joins
                        starterUri(),
                        false,
                        Collections.<FSJoin>emptyList()
                },
                {   // 01: one join
                        tableUriWithJoins(createFSJoin(FSJoin.Type.INNER, 1)),
                        true,
                        Arrays.asList(createFSJoin(FSJoin.Type.INNER, 1))
                },
                {   // 02: two joins
                        tableUriWithJoins(
                                createFSJoin(FSJoin.Type.INNER, 1),
                                createFSJoin(FSJoin.Type.NATURAL, 0)
                        ),
                        true,
                        Arrays.asList(
                                createFSJoin(FSJoin.Type.INNER, 1),
                                createFSJoin(FSJoin.Type.NATURAL, 0)
                        )
                }
        });
    }

    @Test
    public void shouldCorrectlyDetermineWhetherHasJoins() {
        assertEquals(expectedHasJoin, analyzerUnderTest.hasJoin());
    }

    @Test
    public void shouldCorrectlyDetermineJoins() throws ParseException {
        List<FSJoin> actualJoins = analyzerUnderTest.getJoins();
        for (int i = 0; i < expectedJoins.size(); i++) {
            assertFSJoinEquals(expectedJoins.get(i), actualJoins.get(i));
        }
        assertEquals(expectedJoins.size(), actualJoins.size());
    }
}
