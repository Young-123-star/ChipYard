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
class HcpWebhookTest {

    @Autowired MockMvc mockMvc;
    private static final String TOKEN = "dms-demo-integration-token";

    private String payload(String employeeNo) {
        return "{\"employeeNo\":\"" + employeeNo + "\",\"name\":\"新员工\",\"sex\":1,\"empType\":1,"
             + "\"department\":\"研发部\",\"mobile\":\"13900000000\",\"entryDate\":\"2026-07-10\"}";
    }

    @Test
    void hcp_webhook_creates_resident_and_intake() throws Exception {
        mockMvc.perform(post("/api/integration/hcp/employee")
                .header("X-Integration-Token", TOKEN)
                .contentType("application/json").content(payload("E7001")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void hcp_webhook_idempotent_on_same_employee() throws Exception {
        mockMvc.perform(post("/api/integration/hcp/employee")
                .header("X-Integration-Token", TOKEN)
                .contentType("application/json").content(payload("E7002")))
            .andExpect(status().isOk());
        mockMvc.perform(post("/api/integration/hcp/employee")
                .header("X-Integration-Token", TOKEN)
                .contentType("application/json").content(payload("E7002")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void hcp_webhook_rejects_bad_token() throws Exception {
        mockMvc.perform(post("/api/integration/hcp/employee")
                .header("X-Integration-Token", "wrong")
                .contentType("application/json").content(payload("E7003")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40100));
    }
}
