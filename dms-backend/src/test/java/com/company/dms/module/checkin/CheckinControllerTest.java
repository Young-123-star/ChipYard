package com.company.dms.module.checkin;

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
class CheckinControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;

    private String auth() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void list_intakes() throws Exception {
        mockMvc.perform(get("/api/checkin/intakes").header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void create_then_assign_flow() throws Exception {
        String body = "{\"residentId\":3,\"genderLimitReq\":1}";
        String resp = mockMvc.perform(post("/api/checkin/intakes")
                .header("Authorization", auth())
                .contentType("application/json").content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn().getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(resp);
        long intakeId = node.get("data").asLong();

        mockMvc.perform(post("/api/checkin/intakes/" + intakeId + "/assign")
                .header("Authorization", auth())
                .contentType("application/json").content("{\"bedId\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void list_records() throws Exception {
        mockMvc.perform(get("/api/checkin/records").header("Authorization", auth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
