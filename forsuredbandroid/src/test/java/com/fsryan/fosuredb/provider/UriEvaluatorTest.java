package com.fsryan.fosuredb.provider;

import com.fsryan.fosuredb.util.UriUtil;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UriEvaluatorTest {

    @Test
    public void shouldDetectSpecificRecordUri() {
        assertTrue(UriEvaluator.isSpecificRecordUri(UriUtil.specificRecordUri(1L)));
    }

    @Test
    public void shouldDetectNonSpecificRecordUri() {
        assertFalse(UriEvaluator.isSpecificRecordUri(UriUtil.allRecordsUri()));
    }
}