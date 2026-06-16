package com.company.dms.module.resident;

import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ResidentResignTest {

    @Autowired ResidentService residentService;

    @Test
    void mark_resigned_sets_status_zero() {
        residentService.markResigned(2L); // 李四
        Resident r = residentService.getById(2L);
        assertEquals(0, r.getStatus(), "离职状态=0");
    }
}
