package com.company.dms.module.dict;

import com.company.dms.common.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DictControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;

    private String auth() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void dict_items_require_authentication() throws Exception {
        mockMvc.perform(get("/api/dict/items").param("dictType", "ROOM_FACILITY"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void list_enabled_dict_items() throws Exception {
        mockMvc.perform(get("/api/dict/items")
                        .param("dictType", "ROOM_FACILITY")
                        .param("activeOnly", "true")
                        .header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].dictType").value("ROOM_FACILITY"));
    }

    @Test
    void create_and_update_dict_item() throws Exception {
        String response = mockMvc.perform(post("/api/dict/items")
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dictType\":\"ROOM_FACILITY\",\"dictValue\":\"冰箱\",\"dictLabel\":\"冰箱\",\"sortOrder\":99,\"tagType\":\"info\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();

        String id = response.replaceAll(".*\\\"data\\\":(\\d+).*", "$1");

        mockMvc.perform(put("/api/dict/items/" + id)
                        .header("Authorization", auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dictType\":\"ROOM_FACILITY\",\"dictValue\":\"冰箱\",\"dictLabel\":\"小冰箱\",\"sortOrder\":100,\"tagType\":\"success\",\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
