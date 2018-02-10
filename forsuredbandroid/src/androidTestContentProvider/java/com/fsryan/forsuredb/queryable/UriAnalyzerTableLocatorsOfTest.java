package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import com.fsryan.forsuredb.ForSureAndroidInfoFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.fsryan.forsuredb.api.FSJoin.Type.INNER;
import static com.fsryan.forsuredb.TestObjectUtil.createFSJoin;
import static com.fsryan.forsuredb.TestObjectUtil.createSize1FSJoin;
import static com.fsryan.forsuredb.TestObjectUtil.starterUri;
import static com.fsryan.forsuredb.TestObjectUtil.tableUriWithJoins;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class UriAnalyzerTableLocatorsOfTest {

    @BeforeClass
    public static void initForsureAndroidInfoFactory() {
        ForSureAndroidInfoFactory.init(getTargetContext(), BaseQueryableTest.AUTHORITY);
    }

    private final Uri input;
    private final List<Uri> expectedOutput;

    public UriAnalyzerTableLocatorsOfTest(Uri input, List<Uri> expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object [][] {
                {   // 00: no joins should return one uri that is the table queried
                        starterUri(),
                        Arrays.asList(starterUri())
                },
                {   // 01: one join should return two uris that is the table queried and the table joined
                        tableUriWithJoins("child", createFSJoin(INNER, 1)),
                        Arrays.asList(starterUri("child"), starterUri("parent"))
                },
                {   // 02: two joins should return three uris that is the table queried and the tables joined
                        tableUriWithJoins(
                                "child",
                                createFSJoin(INNER, 1),
                                createSize1FSJoin(INNER, "parent", "grandparent")
                        ),
                        Arrays.asList(
                                starterUri("child"),
                                starterUri("parent"),
                                starterUri("grandparent")
                        )
                }
        });
    }

    @Test
    public void shouldCorrectlyDetermineAllTableUris() {
        final List<Uri> actual = UriAnalyzer.tableLocatorsOf(input);
        for (Uri expected : expectedOutput) {
            assertTrue("did not contain expected uri: " + expected + "; had: " + actual, actual.contains(expected));
        }
        assertEquals(expectedOutput.size(), actual.size());
    }
}
