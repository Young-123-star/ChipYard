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
class FeeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    private String auth() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void list_standards() throws Exception {
        mockMvc.perform(get("/api/fee/standards").header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
    }

    @Test
    void list_bills() throws Exception {
        mockMvc.perform(get("/api/fee/bills").header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void generate_then_pay_flow() throws Exception {
        mockMvc.perform(post("/api/fee/bills/generate").header("Authorization", auth())
                .contentType("application/json").content("{\"period\":\"2026-08\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.generated").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
        // 种子账单 id=1 缴费
        mockMvc.perform(post("/api/fee/bills/1/pay").header("Authorization", auth())
                .contentType("application/json").content("{\"payMethod\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void requires_auth() throws Exception {
        mockMvc.perform(get("/api/fee/bills")).andExpect(status().isUnauthorized());
    }
}
