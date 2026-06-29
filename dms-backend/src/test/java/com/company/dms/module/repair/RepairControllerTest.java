package com.company.dms.module.repair;

import com.company.dms.common.security.JwtUtil;
import com.company.dms.module.repair.dto.RepairCreateDTO;
import com.company.dms.module.repair.service.RepairService;
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
class RepairControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired RepairService repairService;

    private String auth() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void requires_auth() throws Exception {
        mockMvc.perform(get("/api/repair/orders")).andExpect(status().isUnauthorized());
    }

    @Test
    void create_accept_complete_flow() throws Exception {
        mockMvc.perform(post("/api/repair/orders").header("Authorization", auth())
                .contentType("application/json").content("{\"roomId\":2,\"residentId\":1,\"title\":\"door broken\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        RepairCreateDTO dto = new RepairCreateDTO();
        dto.setRoomId(2L);
        dto.setResidentId(1L);
        dto.setTitle("light broken");
        Long id = repairService.create(dto);

        mockMvc.perform(post("/api/repair/orders/" + id + "/accept").header("Authorization", auth())
                .contentType("application/json").content("{\"handler\":\"worker a\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/repair/orders/" + id + "/complete").header("Authorization", auth())
                .contentType("application/json").content("{\"result\":\"fixed\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
