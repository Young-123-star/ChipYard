package com.company.dms.common.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private final JwtUtil jwtUtil = new JwtUtil(
            "dms-demo-secret-key-please-change-in-prod-0123456789abcdef", 720);

    @Test
    void generated_token_can_be_parsed_back_to_userId_and_username() {
        String token = jwtUtil.generate(1L, "admin");
        assertEquals(1L, jwtUtil.getUserId(token));
        assertEquals("admin", jwtUtil.getUsername(token));
    }

    @Test
    void tampered_token_is_invalid() {
        String token = jwtUtil.generate(1L, "admin");
        assertFalse(jwtUtil.isValid(token + "x"));
        assertTrue(jwtUtil.isValid(token));
    }
}
