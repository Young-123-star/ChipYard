package com.company.dms.module.exporter;

import com.company.dms.common.security.JwtUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtUtil jwtUtil;

    private String auth() { return "Bearer " + jwtUtil.generate(1L, "admin"); }

    @Test
    void export_requires_auth() throws Exception {
        mockMvc.perform(get("/api/export/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void export_rooms_uses_filters_and_ignores_page_size() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/api/export/rooms")
                        .param("status", "3")
                        .param("page", "1")
                        .param("size", "1")
                        .header("Authorization", auth()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn().getResponse();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            var sheet = workbook.getSheetAt(0);
            assertEquals(1, sheet.getLastRowNum());
            assertEquals("rooms", sheet.getSheetName());
            assertEquals("\u7ef4\u4fee\u4e2d", sheet.getRow(1).getCell(7).getStringCellValue());
        }
    }

    @Test
    void export_residents_returns_xlsx() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/api/export/residents")
                        .param("page", "1")
                        .param("size", "1")
                        .header("Authorization", auth()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            assertEquals("residents", workbook.getSheetAt(0).getSheetName());
            assertTrue(workbook.getSheetAt(0).getLastRowNum() >= 3);
        }
    }

    @Test
    void export_repair_orders_returns_xlsx() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/api/export/repair-orders")
                        .param("page", "1")
                        .param("size", "1")
                        .header("Authorization", auth()))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            assertEquals("repair-orders", workbook.getSheetAt(0).getSheetName());
            assertTrue(workbook.getSheetAt(0).getLastRowNum() >= 1);
        }
    }
}
