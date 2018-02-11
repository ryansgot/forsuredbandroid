package com.fsryan.forsuredb.queryable;

import com.fsryan.forsuredb.api.FSOrdering;
import com.fsryan.forsuredb.api.OrderBy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;

import static com.fsryan.forsuredb.queryable.Assertions.assertFSOrderingEquals;
import static com.fsryan.forsuredb.TestObjectUtil.orderingAsc;
import static com.fsryan.forsuredb.TestObjectUtil.orderingDesc;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class UriAnalyzerFSOrderingTest {

    private final FSOrdering ordering;
    private final String stringified;

    public UriAnalyzerFSOrderingTest(FSOrdering ordering, String stringified) {
        this.ordering = ordering;
        this.stringified = stringified;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object [][] {
                {   // 00: specified ASC means ASC
                        orderingAsc("table", "column"),
                        "table:column:" + OrderBy.ORDER_ASC
                },
                {   // 01: specified DESC means DESC
                        orderingDesc("table", "column"),
                        "table:column:" + OrderBy.ORDER_DESC
                }
        });
    }

    @Test
    public void shouldCorrectlyStringifyOrdering() throws ParseException {
        final String actual = UriAnalyzer.stringify(ordering);
        assertEquals(stringified, actual);
    }

    @Test
    public void shouldCorrectlyDestringifyOrdering() throws ParseException {
        final FSOrdering actual = UriAnalyzer.destringifyFSOrdering(stringified);
        assertFSOrderingEquals(ordering, actual);
    }

    @Test
    public void shouldInverselyStringifyDestringifyOrdering() throws ParseException {
        final String localStringified = UriAnalyzer.stringify(ordering);
        final FSOrdering actual = UriAnalyzer.destringifyFSOrdering(localStringified);
        assertFSOrderingEquals(ordering, actual);
    }

    @Test
    public void shouldInverselyDestringifyStringifyOrdering() throws ParseException {
        final FSOrdering destringified = UriAnalyzer.destringifyFSOrdering(stringified);
        final String actual = UriAnalyzer.stringify(destringified);
        assertEquals(stringified, actual);
    }
}
