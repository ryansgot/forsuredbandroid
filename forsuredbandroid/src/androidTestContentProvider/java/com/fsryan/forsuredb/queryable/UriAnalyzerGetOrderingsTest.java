package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import com.fsryan.forsuredb.api.FSOrdering;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.fsryan.forsuredb.queryable.Assertions.assertFSOrderingEquals;
import static com.fsryan.forsuredb.TestObjectUtil.orderingAsc;
import static com.fsryan.forsuredb.TestObjectUtil.orderingDesc;
import static com.fsryan.forsuredb.TestObjectUtil.starterUri;
import static com.fsryan.forsuredb.TestObjectUtil.tableUriWithOrderings;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriAnalyzerGetOrderingsTest extends BaseUriAnalyzerTest {

    private final boolean expectedHasOrderings;
    private final List<FSOrdering> expectedOrderings;

    public UriAnalyzerGetOrderingsTest(Uri input, boolean expectedHasOrderings, List<FSOrdering> expectedOrderings) {
        super(input);
        this.expectedHasOrderings = expectedHasOrderings;
        this.expectedOrderings = expectedOrderings;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {   // 00: no orderings should return empty orderings
                        starterUri(),
                        false,
                        Collections.<FSOrdering>emptyList()
                },
                {   // 01: one ordering should return the ordering in a one-item list
                        tableUriWithOrderings(orderingAsc()),
                        true,
                        Arrays.asList(orderingAsc())
                },
                {   // 02: two orderings should return the ordering in a two-item list
                        tableUriWithOrderings(
                                orderingAsc("table", "column1"),
                                orderingDesc("table", "column2")
                        ),
                        true,
                        Arrays.asList(
                                orderingAsc("table", "column1"),
                                orderingDesc("table", "column2")
                        )
                }
        });
    }

    @Test
    public void shouldCorrectlyDetermineWhetherHasOrderings() {
        assertEquals(expectedHasOrderings, analyzerUnderTest.hasOrdering());
    }

    @Test
    public void shouldCorrectlyDetermineLimits() throws ParseException {
        List<FSOrdering> actualOrderings = analyzerUnderTest.getOrderings();
        for (int i = 0; i < expectedOrderings.size(); i++) {
            assertFSOrderingEquals(expectedOrderings.get(i), actualOrderings.get(i));
        }
        assertEquals(expectedOrderings.size(), actualOrderings.size());
    }
}
