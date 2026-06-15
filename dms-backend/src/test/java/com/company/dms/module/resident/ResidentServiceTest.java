package com.company.dms.module.resident;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.resident.dto.ResidentSaveDTO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ResidentServiceTest {

    @Autowired ResidentService residentService;

    @Test
    void create_then_upsert_by_employee_no_is_idempotent() {
        ResidentSaveDTO dto = new ResidentSaveDTO();
        dto.setEmployeeNo("E9001");
        dto.setRealName("测试员工");
        dto.setGender(1);
        dto.setResidentType(1);
        dto.setStatus(1);
        Long id1 = residentService.upsertByEmployeeNo(dto, 2);
        Long id2 = residentService.upsertByEmployeeNo(dto, 2);
        assertEquals(id1, id2, "相同工号 upsert 应返回同一 id");
    }

    @Test
    void create_duplicate_employee_no_rejected() {
        ResidentSaveDTO dto = new ResidentSaveDTO();
        dto.setEmployeeNo("E1001"); // 种子已存在
        dto.setRealName("重复工号");
        assertThrows(BizException.class, () -> residentService.create(dto));
    }

    @Test
    void get_missing_resident_throws() {
        assertThrows(BizException.class, () -> residentService.getById(999999L));
    }
}
