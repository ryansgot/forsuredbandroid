package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.Limits;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;

import static com.fsryan.forsuredb.queryable.Assertions.assertLimitsEquals;
import static com.fsryan.forsuredb.TestObjectUtil.createLimits;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriAnalyzerLimitsTest {

    private final Limits limits;
    private final String stringified;

    public UriAnalyzerLimitsTest(Limits limits, String stringified) {
        this.limits = limits;
        this.stringified = stringified;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object [][] {
                {   // 00: zeroes for limits, not from bottom
                        createLimits(0, 0, false),
                        "0:0:false"
                },
                {   // 01: 1 count, 1 offset, from bottom
                        createLimits(1, 1, true),
                        "1:1:true"
                },
                {   // 02: large numbers
                        createLimits(234534, 76854543, true),
                        "234534:76854543:true"
                }
        });
    }

    @Test
    public void shouldInverselyStringifyAndDestringify() throws ParseException {
        final String localStringified = UriAnalyzer.stringify(limits);
        assertLimitsEquals(limits, UriAnalyzer.destringifyLimits(localStringified));
    }

    @Test
    public void shouldCorrectlyDestringifyFSJoin() throws ParseException {
        assertLimitsEquals(limits, UriAnalyzer.destringifyLimits(stringified));
    }

    @Test
    public void shouldCorrectlyStringify() {
        assertEquals(stringified, UriAnalyzer.stringify(limits));
    }

    @Test
    public void shouldInversityDestringifyAndStringify() throws ParseException {
        final Limits destringified = UriAnalyzer.destringifyLimits(stringified);
        assertEquals(stringified, UriAnalyzer.stringify(destringified));
    }
}
