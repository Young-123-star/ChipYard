package com.company.dms.module.resident;

import com.company.dms.common.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResidentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;

    @Test
    void page_residents_returns_seeded() throws Exception {
        mockMvc.perform(get("/api/residents")
                .header("Authorization", "Bearer " + jwtUtil.generate(1L, "admin")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));
    }

    @Test
    void page_residents_requires_auth() throws Exception {
        mockMvc.perform(get("/api/residents"))
            .andExpect(status().isUnauthorized());
    }
}
