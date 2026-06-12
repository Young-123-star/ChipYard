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
class FloorControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;

    @Test
    void list_floors_of_building_one() throws Exception {
        mockMvc.perform(get("/api/floors").param("buildingId", "1")
                .header("Authorization", "Bearer " + jwtUtil.generate(1L, "admin")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void floor_stats_are_computed_from_rooms() throws Exception {
        // 种子数据：1层有 A101(2床,0住) + A102(2床,1住) → roomCount=2, bedCount=4, occupiedBeds=1
        mockMvc.perform(get("/api/floors").param("buildingId", "1")
                .header("Authorization", "Bearer " + jwtUtil.generate(1L, "admin")))
            .andExpect(jsonPath("$.data[0].floorNumber").value(1))
            .andExpect(jsonPath("$.data[0].roomCount").value(2))
            .andExpect(jsonPath("$.data[0].bedCount").value(4))
            .andExpect(jsonPath("$.data[0].occupiedBeds").value(1));
    }
}
