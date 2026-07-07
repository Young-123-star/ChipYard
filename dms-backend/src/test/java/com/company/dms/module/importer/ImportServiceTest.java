package com.company.dms.module.importer;

import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.service.BedService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ImportServiceTest {

    @Autowired ImportService importService;
    @Autowired ResidentService residentService;
    @Autowired CheckinService checkinService;
    @Autowired BedService bedService;

    @Test
    void resource_validate_rejects_duplicate_room_in_file() throws Exception {
        byte[] file = workbook(
                headers("buildingCode", "buildingName", "address", "floorNumber", "floorName", "roomNumber", "roomType", "bedCount", "bedNumbers", "genderLimit", "area", "orientation", "facilities", "status"),
                row("T", "Test Building", "Park", 1, "F1", "101", 2, 2, "", 0, 20, "S", "", 1),
                row("T", "Test Building", "Park", 1, "F1", "101", 2, 2, "", 0, 20, "S", "", 1));

        ImportResult result = importService.validate("resource", file);

        assertFalse(result.isSuccess());
        assertEquals(2, result.getTotalRows());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("roomNumber")));
    }

    @Test
    void resident_execute_upserts_by_employee_no() throws Exception {
        byte[] file = workbook(
                headers("employeeNo", "realName", "gender", "residentType", "deptName", "phone", "idCard", "status"),
                row("IMP001", "Import User", 1, 1, "QA", "13900000001", "ID001", 1));

        ImportResult result = importService.execute("resident", file);

        assertTrue(result.isSuccess());
        Resident resident = residentService.getByEmployeeNo("IMP001");
        assertNotNull(resident);
        assertEquals("Import User", resident.getRealName());
        assertEquals("QA", resident.getDeptName());
    }

    @Test
    void checkin_record_execute_occupies_bed_and_creates_record() throws Exception {
        byte[] file = workbook(
                headers("employeeNo", "buildingCode", "roomNumber", "bedNumber", "checkinDate", "remark"),
                row("E2001", "A", "A101", "B", "2026-07-01", "seed import"));

        ImportResult result = importService.execute("checkin-record", file);

        assertTrue(result.isSuccess(), result.getErrors().toString());
        assertNotNull(checkinService.findActiveRecordByResident(3L));
        Bed bed = bedService.listByRoom(1L).stream()
                .filter(b -> b.getBedNumber().equals("B"))
                .findFirst()
                .orElseThrow();
        assertEquals(2, bed.getStatus());
        assertEquals(3L, bed.getCurrentUserId());
    }

    @Test
    void checkin_record_validate_rejects_duplicate_employee_in_file() throws Exception {
        byte[] file = workbook(
                headers("employeeNo", "buildingCode", "roomNumber", "bedNumber", "checkinDate", "remark"),
                row("E2001", "A", "A101", "A", "2026-07-01", "seed import"),
                row("E2001", "A", "A101", "B", "2026-07-01", "seed import"));

        ImportResult result = importService.validate("checkin-record", file);

        assertFalse(result.isSuccess());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("employeeNo")));
    }
    private static Object[] headers(String... values) {
        return values;
    }

    private static Object[] row(Object... values) {
        return values;
    }

    private static byte[] workbook(Object[] headers, Object[]... rows) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("data");
            write(sheet.createRow(0), headers);
            for (int i = 0; i < rows.length; i++) {
                write(sheet.createRow(i + 1), rows[i]);
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static void write(org.apache.poi.ss.usermodel.Row row, Object[] values) {
        for (int i = 0; i < values.length; i++) {
            var cell = row.createCell(i);
            if (values[i] instanceof Number n) {
                cell.setCellValue(n.doubleValue());
            } else {
                cell.setCellValue(String.valueOf(values[i]));
            }
        }
    }
}


