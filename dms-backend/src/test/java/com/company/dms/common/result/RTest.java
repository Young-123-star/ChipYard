package com.company.dms.common.result;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RTest {
    @Test
    void ok_wraps_data_with_code_zero() {
        R<String> r = R.ok("hello");
        assertEquals(0, r.getCode());
        assertEquals("hello", r.getData());
        assertEquals("success", r.getMessage());
    }

    @Test
    void fail_carries_code_and_message() {
        R<Void> r = R.fail(40000, "bad request");
        assertEquals(40000, r.getCode());
        assertEquals("bad request", r.getMessage());
        assertNull(r.getData());
    }
}
