package com.company.dms.module.checkin;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.checkin.dto.CreateIntakeCommand;
import com.company.dms.module.checkin.dto.IntakeCreateDTO;
import com.company.dms.module.checkin.service.CheckinService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CheckinIntakeServiceTest {

    @Autowired CheckinService checkinService;

    @Test
    void manual_create_intake() {
        IntakeCreateDTO dto = new IntakeCreateDTO();
        dto.setResidentId(3L);
        Long id = checkinService.createManualIntake(dto);
        assertNotNull(id);
    }

    @Test
    void create_from_command_is_idempotent_by_biz_no() {
        CreateIntakeCommand cmd = new CreateIntakeCommand();
        cmd.setBizNo("OA-DUP-1");
        cmd.setSource(1);
        cmd.setResidentId(3L);
        Long id1 = checkinService.createIntakeFromCommand(cmd);
        Long id2 = checkinService.createIntakeFromCommand(cmd);
        assertEquals(id1, id2, "相同 bizNo 重复推送应幂等返回同一 id");
    }

    @Test
    void cancel_intake() {
        IntakeCreateDTO dto = new IntakeCreateDTO();
        dto.setResidentId(3L);
        Long id = checkinService.createManualIntake(dto);
        checkinService.cancel(id);
        assertEquals(3, checkinService.getIntake(id).getStatus(), "取消后状态=3");
    }

    @Test
    void cancel_already_checked_in_rejected() {
        // 种子 intake id=1 已入住(status=2)
        assertThrows(BizException.class, () -> checkinService.cancel(1L));
    }
}
