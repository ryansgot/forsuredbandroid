package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import com.fsryan.forsuredb.api.Limits;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;

import static com.fsryan.forsuredb.queryable.Assertions.assertLimitsEquals;
import static com.fsryan.forsuredb.TestObjectUtil.createLimits;
import static com.fsryan.forsuredb.TestObjectUtil.starterUri;
import static com.fsryan.forsuredb.TestObjectUtil.tableUriWithLimits;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriAnalyzerGetLimitsTest extends BaseUriAnalyzerTest {

    // workaround for the fact that Parameterized Runner on Android 22, the input argument null
    // is being detected as the string "null". This, for some reason, does not happen on Android24+
    private static final Limits NULL_LIMITS_HACK = new Limits() {
        @Override
        public int count() {
            return Integer.MIN_VALUE;
        }

        @Override
        public int offset() {
            return Integer.MIN_VALUE;
        }

        @Override
        public boolean isBottom() {
            return false;
        }
    };

    private final boolean expectedHasLimits;
    private final Limits expectedLimits;

    public UriAnalyzerGetLimitsTest(Uri input, boolean expectedHasLimits, Limits expectedLimits) {
        super(input);
        this.expectedHasLimits = expectedHasLimits;
        if (NULL_LIMITS_HACK.offset() == expectedLimits.offset()
            && NULL_LIMITS_HACK.count() == expectedLimits.offset()
            && NULL_LIMITS_HACK.isBottom() == expectedLimits.isBottom()) {
            this.expectedLimits = null;
        } else {
            this.expectedLimits = expectedLimits;
        }
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: no limits should return null limits
                        starterUri(),
                        false,
                        NULL_LIMITS_HACK
                },
                {   // 01: zero limits should return zero limits
                        tableUriWithLimits(createLimits(0, 0, false)),
                        true,
                        createLimits(0, 0, false)
                },
                {   // 02: 1 count 0 offset limit should return 1 count limit
                        tableUriWithLimits(createLimits(1, 0, false)),
                        true,
                        createLimits(1, 0, false)
                },
                {   // 03: 0 count 1 offset limit should return 0 count 1 offset limit
                        tableUriWithLimits(createLimits(0, 1, false)),
                        true,
                        createLimits(0, 1, false)
                },
                {   // 04: 1 count 1 offset limit should return 1 count 1 offset limit
                        tableUriWithLimits(createLimits(1, 1, false)),
                        true,
                        createLimits(1, 1, false)
                },
                {   // 05: 1 count 1 offset limit from bottom should return 1 count 1 offset limit form bottom
                        tableUriWithLimits(createLimits(1, 1, true)),
                        true,
                        createLimits(1, 1, true)
                },
                {   // 06: -1 count 1 offset limit from bottom should return 1 count 1 offset limit form bottom
                        tableUriWithLimits(createLimits(-1, 1, true)),
                        true,
                        createLimits(-1, 1, true)
                }
        });
    }

    @Test
    public void shouldCorrectlyDetermineWhetherHasLimits() {
        assertEquals(expectedHasLimits, analyzerUnderTest.hasLimits());
    }

    @Test
    public void shouldCorrectlyDetermineLimits() throws ParseException {
        assertLimitsEquals(expectedLimits, analyzerUnderTest.getLimits());
    }
}
