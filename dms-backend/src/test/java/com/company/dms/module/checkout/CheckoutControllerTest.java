package com.company.dms.module.checkout;

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
class CheckoutControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    private String auth() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void list_orders() throws Exception {
        mockMvc.perform(get("/api/checkout/orders").header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    @Test
    void confirm_seed_order_flow() throws Exception {
        mockMvc.perform(post("/api/checkout/orders/1/confirm")
                .header("Authorization", auth())
                .contentType("application/json").content("{\"checkoutDate\":\"2026-07-01\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void requires_auth() throws Exception {
        mockMvc.perform(get("/api/checkout/orders")).andExpect(status().isUnauthorized());
    }
}
