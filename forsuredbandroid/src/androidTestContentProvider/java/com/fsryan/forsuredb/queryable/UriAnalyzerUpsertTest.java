package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static com.fsryan.forsuredb.TestObjectUtil.makeUpsert;
import static com.fsryan.forsuredb.TestObjectUtil.starterUri;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriAnalyzerUpsertTest extends BaseUriAnalyzerTest {

    private final boolean expectedUpsert;

    public UriAnalyzerUpsertTest(Uri input, boolean expectedUpsert) {
        super(input);
        this.expectedUpsert = expectedUpsert;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: non-upsert Uri
                        starterUri(),
                        false,
                },
                {   // 01: upsert Uri
                        makeUpsert(starterUri()).build(),
                        true
                }
        });
    }

    @Test
    public void shouldCorrectlyDetermineWhetherDistinct() {
        assertEquals(expectedUpsert, analyzerUnderTest.isUpsert());
    }
}
