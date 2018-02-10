package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Limits;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;

import static com.fsryan.forsuredb.queryable.Assertions.assertFSSelectionEquals;
import static com.fsryan.forsuredb.TestObjectUtil.addLimitsTo;
import static com.fsryan.forsuredb.TestObjectUtil.createLimits;
import static com.fsryan.forsuredb.TestObjectUtil.starterUri;

@RunWith(Parameterized.class)
public class UriAnalyzerGetFSSelectionTest extends BaseUriAnalyzerTest {

    private final String inputSelection;
    private final String[] inputSelectionArgs;
    private final FSSelection expectedFSSelection;

    public UriAnalyzerGetFSSelectionTest(Uri inputUri,
                                         String inputSelection,
                                         String[] inputSelectionArgs,
                                         final String expectedSelection,
                                         final String[] expectedSelectionArgs,
                                         final Limits expectedLimits) {
        super(inputUri);
        this.inputSelection = inputSelection;
        this.inputSelectionArgs = inputSelectionArgs;
        this.expectedFSSelection = new FSSelection() {
            @Override
            public String where() {
                return expectedSelection;
            }

            @Override
            public String[] replacements() {
                return expectedSelectionArgs;
            }

            @Override
            public Limits limits() {
                return expectedLimits;
            }
        };
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object [][] {
                {   // 00: table uri without selection or selection args
                        starterUri(),
                        null,
                        null,
                        null,
                        null,
                        null
                },
                {   // 01: specific record uri without selection or selection args
                        starterUri(1L),
                        null,
                        null,
                        "_id = ?",
                        new String[] {"1"},
                        null
                },
                {   // 02: specific record uri with selection and selection args
                        starterUri(982374L),
                        "column1 = ?",
                        new String[] {"something"},
                        "_id = ? AND (column1 = ?)",
                        new String[] {"982374", "something"},
                        null
                },
                {   // 03: specific record uri with selection and selection args and limits
                        addLimitsTo(starterUri(982374L), createLimits(1, 3, true)).build(),
                        "column1 = ?",
                        new String[] {"something"},
                        "_id = ? AND (column1 = ?)",
                        new String[] {"982374", "something"},
                        createLimits(1, 3, true)
                },
                {   // 04: table uri with other selection arguments
                        starterUri(),
                        "column1 = ?",
                        new String[] {"something"},
                        "column1 = ?",
                        new String[] {"something"},
                        null
                },
                {   // 05: table uri with other selection arguments
                        addLimitsTo(starterUri(), createLimits(5, 2, false)).build(),
                        "column1 = ?",
                        new String[] {"something"},
                        "column1 = ?",
                        new String[] {"something"},
                        createLimits(5, 2, false)
                }
        });
    }

    @Test
    public void shouldCorrectlyAnalyzeSelection() throws ParseException {
        final FSSelection actual = analyzerUnderTest.getSelection(inputSelection, inputSelectionArgs);
        assertFSSelectionEquals(expectedFSSelection, actual);
    }
}
