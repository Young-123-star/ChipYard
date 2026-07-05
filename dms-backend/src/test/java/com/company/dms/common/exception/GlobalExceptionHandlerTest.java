package com.company.dms.common.exception;

import com.company.dms.common.result.R;
import com.company.dms.common.result.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GlobalExceptionHandlerTest {

    @Test
    void handleOther_returnsGenericSystemErrorMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        String internalMessage = new String(new char[] {'s', 'e', 'c', 'r', 'e', 't'});

        R<Void> r = handler.handleOther(new RuntimeException(internalMessage));

        assertEquals(ResultCode.SYSTEM_ERROR.getCode(), r.getCode());
        assertEquals(ResultCode.SYSTEM_ERROR.getMessage(), r.getMessage());
        assertNotEquals(internalMessage, r.getMessage());
    }
}