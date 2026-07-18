package com.company.dms.module.fee;

import com.company.dms.common.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MeterControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    private String auth() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void get_rate() throws Exception {
        mockMvc.perform(get("/api/fee/utility-rate").header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.electricityPrice").value(0.5383));
    }

    @Test
    void save_reading_then_list() throws Exception {
        mockMvc.perform(post("/api/fee/meter-readings").header("Authorization", auth())
                .contentType("application/json")
                .content("{\"roomId\":2,\"period\":\"2026-07\",\"meterType\":1,\"currentReading\":160.00}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/api/fee/meter-readings").param("period", "2026-07").header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void generate_utility_bills() throws Exception {
        mockMvc.perform(post("/api/fee/utility-bills/generate").header("Authorization", auth())
                .contentType("application/json").content("{\"period\":\"2026-06\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.generated").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void requires_auth() throws Exception {
        mockMvc.perform(get("/api/fee/meter-readings")).andExpect(status().isUnauthorized());
    }
}
