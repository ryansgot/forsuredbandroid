package com.fsryan.forsuredb.queryable;

import android.net.Uri;

import com.fsryan.forsuredb.api.FSSelection;
import com.fsryan.forsuredb.api.Limits;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;

import static com.fsryan.forsuredb.TestQueryUtil.selection;
import static com.fsryan.forsuredb.queryable.Assertions.assertFSSelectionEquals;
import static com.fsryan.forsuredb.TestObjectUtil.addLimitsTo;
import static com.fsryan.forsuredb.TestObjectUtil.createLimits;
import static com.fsryan.forsuredb.TestObjectUtil.starterUri;

@RunWith(Parameterized.class)
public class UriAnalyzerGetFSSelectionTest extends BaseUriAnalyzerTest {

    private final String inputSelection;
    private final String[] inputSelectionArgs;
    private final FSSelection expectedSelection;

    public UriAnalyzerGetFSSelectionTest(Uri inputUri,
                                         String inputSelection,
                                         String[] inputSelectionArgs,
                                         FSSelection expectedSelection) {
        super(inputUri);
        this.inputSelection = inputSelection;
        this.inputSelectionArgs = inputSelectionArgs;
        this.expectedSelection = expectedSelection;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object [][] {
                {   // 00: table uri without selection or selection args
                        starterUri(),
                        null,
                        (String[]) null,
                        selection().build()
                },
                {   // 01: specific record uri without selection or selection args
                        starterUri(1L),
                        null,
                        (String[]) null,
                        selection().where("_id = ?", new String[] {"1"}).build()
                },
                {   // 02: specific record uri with selection and selection args
                        starterUri(982374L),
                        "column1 = ?",
                        new String[] {"something"},
                        selection().where("_id = ? AND (column1 = ?)", new String[] {"982374", "something"}).build()
                },
                {   // 03: specific record uri with selection and selection args and limits
                        addLimitsTo(starterUri(982374L), createLimits(1, 3, true)).build(),
                        "column1 = ?",
                        new String[] {"something"},
                        selection()
                                .where("_id = ? AND (column1 = ?)", new String[] {"982374", "something"})
                                .limitCount(1)
                                .offset(3)
                                .fromBottom(true)
                                .build(),                },
                {   // 04: table uri with other selection arguments
                        starterUri(),
                        "column1 = ?",
                        new String[] {"something"},
                        selection().where("column1 = ?", new String[] {"something"}).build()
                },
                {   // 05: table uri with other selection arguments
                        addLimitsTo(starterUri(), createLimits(5, 2, false)).build(),
                        "column1 = ?",
                        new String[] {"something"},
                        selection()
                                .where("column1 = ?", new String[] {"something"})
                                .fromBottom(false)
                                .limitCount(5)
                                .offset(2)
                                .build()
                }
        });
    }

    @Test
    public void shouldCorrectlyAnalyzeSelection() throws ParseException {
        final FSSelection actual = analyzerUnderTest.getSelection(inputSelection, inputSelectionArgs);
        assertFSSelectionEquals(expectedSelection, actual);
    }
}
