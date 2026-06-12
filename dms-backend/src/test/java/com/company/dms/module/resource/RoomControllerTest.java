package com.company.dms.module.resource;

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
class RoomControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;

    private String bearer() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void page_filter_by_floor() throws Exception {
        mockMvc.perform(get("/api/rooms").param("floorId", "1").header("Authorization", bearer()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    void page_filter_by_status_maintenance() throws Exception {
        mockMvc.perform(get("/api/rooms").param("status", "3").header("Authorization", bearer()))
            .andExpect(jsonPath("$.data.total").value(1));
    }
}
