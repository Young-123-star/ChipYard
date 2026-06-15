package com.company.dms.module.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OaWebhookTest {

    @Autowired MockMvc mockMvc;

    private static final String TOKEN = "dms-demo-integration-token";

    private String payload(String applicationNo) {
        return "{\"applicationNo\":\"" + applicationNo + "\",\"employeeNo\":\"E1002\",\"employeeName\":\"李四\","
             + "\"gender\":2,\"expectDate\":\"2026-07-01\",\"genderLimit\":2,\"roomType\":2,\"buildingId\":1,\"note\":\"OA来\"}";
    }

    @Test
    void oa_webhook_creates_intake() throws Exception {
        mockMvc.perform(post("/api/integration/oa/checkin-application")
                .header("X-Integration-Token", TOKEN)
                .contentType("application/json").content(payload("OA-T-1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void oa_webhook_idempotent_on_same_application_no() throws Exception {
        mockMvc.perform(post("/api/integration/oa/checkin-application")
                .header("X-Integration-Token", TOKEN)
                .contentType("application/json").content(payload("OA-T-2")))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/integration/oa/checkin-application")
                .header("X-Integration-Token", TOKEN)
                .contentType("application/json").content(payload("OA-T-2")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void oa_webhook_rejects_bad_token() throws Exception {
        mockMvc.perform(post("/api/integration/oa/checkin-application")
                .header("X-Integration-Token", "wrong")
                .contentType("application/json").content(payload("OA-T-3")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40100));
    }
}
