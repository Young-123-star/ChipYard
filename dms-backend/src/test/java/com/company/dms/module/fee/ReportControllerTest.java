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
class ReportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    private String auth() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void period_summary_ok() throws Exception {
        mockMvc.perform(get("/api/report/period-summary").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void building_summary_ok() throws Exception {
        mockMvc.perform(get("/api/report/building-summary").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void arrears_ranking_ok_with_limit() throws Exception {
        mockMvc.perform(get("/api/report/arrears-ranking").param("limit", "5").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void usage_trend_ok() throws Exception {
        mockMvc.perform(get("/api/report/usage-trend").header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void requires_auth() throws Exception {
        mockMvc.perform(get("/api/report/period-summary")).andExpect(status().isUnauthorized());
    }
}
