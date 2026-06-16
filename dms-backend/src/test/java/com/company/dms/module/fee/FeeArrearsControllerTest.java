package com.company.dms.module.fee;

import com.company.dms.common.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FeeArrearsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    private String auth() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void arrears_returns_count_and_total() throws Exception {
        mockMvc.perform(get("/api/fee/arrears").param("checkinRecordId", "1").header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.count").value(1))
            .andExpect(jsonPath("$.data.totalAmount").value(800.00));
    }

    @Test
    void arrears_requires_auth() throws Exception {
        mockMvc.perform(get("/api/fee/arrears").param("checkinRecordId", "1"))
            .andExpect(status().isUnauthorized());
    }
}
