package com.company.dms.module.checkin;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.checkin.dto.AssignDTO;
import com.company.dms.module.checkin.dto.IntakeCreateDTO;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.service.BedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CheckinAssignServiceTest {

    @Autowired CheckinService checkinService;
    @Autowired BedService bedService;

    @Test
    void assign_confirms_checkin_and_occupies_bed() {
        // 居住人3(王五,男) 新建意向单
        IntakeCreateDTO dto = new IntakeCreateDTO();
        dto.setResidentId(3L);
        dto.setGenderLimitReq(1);
        Long intakeId = checkinService.createManualIntake(dto);

        // A101(room1, gender_limit=1男) 的空床 bed id=2
        AssignDTO assign = new AssignDTO();
        assign.setBedId(2L);
        Long recordId = checkinService.assign(intakeId, assign);

        assertNotNull(recordId);
        assertEquals(2, checkinService.getIntake(intakeId).getStatus(), "意向单已入住");
        Bed bed = bedService.getById(2L);
        assertEquals(2, bed.getStatus(), "床位已占用");
        assertEquals(3L, bed.getCurrentUserId());
    }

    @Test
    void assign_to_occupied_bed_rejected() {
        IntakeCreateDTO dto = new IntakeCreateDTO();
        dto.setResidentId(3L);
        Long intakeId = checkinService.createManualIntake(dto);
        AssignDTO assign = new AssignDTO();
        assign.setBedId(3L); // 种子 bed3 已入住
        assertThrows(BizException.class, () -> checkinService.assign(intakeId, assign));
    }

    @Test
    void assign_gender_mismatch_rejected() {
        // 居住人2(李四,女) 分到 A101(gender_limit=1男) 的空床 bed1 → 拒绝
        IntakeCreateDTO dto = new IntakeCreateDTO();
        dto.setResidentId(2L);
        Long intakeId = checkinService.createManualIntake(dto);
        AssignDTO assign = new AssignDTO();
        assign.setBedId(1L);
        assertThrows(BizException.class, () -> checkinService.assign(intakeId, assign));
    }
}
