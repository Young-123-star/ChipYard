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
class RoomBoardTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;

    @Test
    void board_of_building_one_returns_four_rooms_with_status() throws Exception {
        mockMvc.perform(get("/api/rooms/board").param("buildingId", "1")
                .header("Authorization", "Bearer " + jwtUtil.generate(1L, "admin")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.length()").value(4))
            .andExpect(jsonPath("$.data[0].roomNumber").isNotEmpty())
            .andExpect(jsonPath("$.data[0].status").isNumber());
    }
}
