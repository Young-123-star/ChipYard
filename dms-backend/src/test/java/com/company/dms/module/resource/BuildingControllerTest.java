package com.company.dms.module.resource;

import com.company.dms.common.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BuildingControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @Autowired JwtUtil jwtUtil;

    private String bearer() {
        return "Bearer " + jwtUtil.generate(1L, "admin");
    }

    private long createBuilding(String code, String name) throws Exception {
        String body = om.writeValueAsString(Map.of("buildingCode", code, "buildingName", name, "floorCount", 5));
        String resp = mockMvc.perform(post("/api/buildings").header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = om.readTree(resp);
        return node.get("data").asLong();
    }

    @Test
    void list_returns_seed_building() throws Exception {
        mockMvc.perform(get("/api/buildings").header("Authorization", bearer()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void create_then_get_returns_building() throws Exception {
        long id = createBuilding("B", "B栋");
        mockMvc.perform(get("/api/buildings/" + id).header("Authorization", bearer()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.buildingName").value("B栋"));
    }

    @Test
    void create_missing_name_returns_param_error() throws Exception {
        String body = om.writeValueAsString(Map.of("buildingCode", "C", "floorCount", 3));
        mockMvc.perform(post("/api/buildings").header("Authorization", bearer())
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    void list_without_token_unauthorized() throws Exception {
        mockMvc.perform(get("/api/buildings"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void deleted_building_is_filtered_out_by_soft_delete() throws Exception {
        long id = createBuilding("DEL", "待删楼栋");
        // delete (soft)
        mockMvc.perform(delete("/api/buildings/" + id).header("Authorization", bearer()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
        // get by id should now be NOT_FOUND (40400) because logic-delete filters it
        mockMvc.perform(get("/api/buildings/" + id).header("Authorization", bearer()))
            .andExpect(jsonPath("$.code").value(40400));
    }
}
