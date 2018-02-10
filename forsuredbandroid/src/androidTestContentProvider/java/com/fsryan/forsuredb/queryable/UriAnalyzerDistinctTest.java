package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static com.fsryan.forsuredb.TestObjectUtil.makeDistinct;
import static com.fsryan.forsuredb.TestObjectUtil.starterUri;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriAnalyzerDistinctTest extends BaseUriAnalyzerTest {

    private final boolean expectedDistinct;

    public UriAnalyzerDistinctTest(Uri input, boolean expectedDistinct) {
        super(input);
        this.expectedDistinct = expectedDistinct;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: non-distinct Uri
                        starterUri(),
                        false,
                },
                {   // 01: distinct Uri
                        makeDistinct(starterUri()).build(),
                        true
                }
        });
    }

    @Test
    public void shouldCorrectlyDetermineWhetherDistinct() {
        assertEquals(expectedDistinct, analyzerUnderTest.isDistinct());
    }
}
