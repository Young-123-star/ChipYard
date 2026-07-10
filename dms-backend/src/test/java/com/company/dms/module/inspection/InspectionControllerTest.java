package com.company.dms.module.inspection;

import com.company.dms.common.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InspectionControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;

    private String auth() {
        return "Bearer " + jwtUtil.generate(1L, "admin");
    }

    @Test
    void requires_auth() throws Exception {
        mockMvc.perform(get("/api/inspection/plans")).andExpect(status().isUnauthorized());
    }

    @Test
    void create_generate_and_read_structured_task() throws Exception {
        String create = "{\"planName\":\"controller plan\",\"cycleType\":1,\"targetType\":1,\"targetId\":1," +
                "\"inspector\":\"admin\",\"items\":[\"消防设施\"]}";
        String body = mockMvc.perform(post("/api/inspection/plans").header("Authorization", auth())
                        .contentType("application/json").content(create))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"data\":(\\d+).*", "$1");

        mockMvc.perform(post("/api/inspection/plans/" + id + "/tasks").header("Authorization", auth())
                        .contentType("application/json").content("{\"plannedDate\":\"2026-08-10\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isNumber());

        mockMvc.perform(get("/api/inspection/tasks").header("Authorization", auth())
                        .param("planId", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].items[0]").value("消防设施"))
                .andExpect(jsonPath("$.data.records[0].results").isArray());
    }
}
