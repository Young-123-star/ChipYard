package com.company.dms.module.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OaCheckoutWebhookTest {

    @Autowired MockMvc mockMvc;
    private static final String TOKEN = "dms-demo-integration-token";

    @Test
    void checkout_application_for_active_resident_creates_order() throws Exception {
        String body = "{\"applicationNo\":\"CO-T-1\",\"employeeNo\":\"E1001\",\"reason\":\"离园\",\"expectDate\":\"2026-07-05\"}";
        mockMvc.perform(post("/api/integration/oa/checkout-application")
                .header("X-Integration-Token", TOKEN).contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.outcome").value("ORDER_CREATED"));
    }

    @Test
    void checkout_application_unknown_employee_returns_no_resident() throws Exception {
        String body = "{\"applicationNo\":\"CO-T-2\",\"employeeNo\":\"E9999\",\"reason\":\"x\"}";
        mockMvc.perform(post("/api/integration/oa/checkout-application")
                .header("X-Integration-Token", TOKEN).contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.outcome").value("NO_RESIDENT"));
    }

    @Test
    void checkout_application_no_active_checkin_rejected() throws Exception {
        String body = "{\"applicationNo\":\"CO-T-3\",\"employeeNo\":\"E2001\",\"reason\":\"x\"}";
        mockMvc.perform(post("/api/integration/oa/checkout-application")
                .header("X-Integration-Token", TOKEN).contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.outcome").value("NO_ACTIVE_CHECKIN"));
    }

    @Test
    void resignation_active_resident_creates_order_and_resigns() throws Exception {
        String body = "{\"resignationNo\":\"RES-T-1\",\"employeeNo\":\"E1001\",\"lastWorkDate\":\"2026-07-10\"}";
        mockMvc.perform(post("/api/integration/oa/resignation")
                .header("X-Integration-Token", TOKEN).contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.outcome").value("ORDER_CREATED"))
            .andExpect(jsonPath("$.data.residentResigned").value(true));
    }

    @Test
    void resignation_no_active_checkin_marks_resigned_no_order() throws Exception {
        String body = "{\"resignationNo\":\"RES-T-2\",\"employeeNo\":\"E2001\",\"lastWorkDate\":\"2026-07-10\"}";
        mockMvc.perform(post("/api/integration/oa/resignation")
                .header("X-Integration-Token", TOKEN).contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.outcome").value("RESIGNED_NO_CHECKIN"))
            .andExpect(jsonPath("$.data.residentResigned").value(true));
    }

    @Test
    void rejects_bad_token() throws Exception {
        mockMvc.perform(post("/api/integration/oa/checkout-application")
                .header("X-Integration-Token", "wrong").contentType("application/json")
                .content("{\"applicationNo\":\"x\",\"employeeNo\":\"E1001\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40100));
    }
}
