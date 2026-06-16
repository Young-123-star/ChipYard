package com.company.dms.module.checkin;

import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.service.CheckinService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CheckinListActiveTest {

    @Autowired CheckinService checkinService;

    @Test
    void list_active_records_returns_only_in_residence() {
        // 种子：张三 record1 在住(status=1)
        List<CheckinRecord> actives = checkinService.listActiveRecords();
        assertFalse(actives.isEmpty(), "应至少有张三一条在住档案");
        assertTrue(actives.stream().allMatch(r -> r.getStatus() == 1), "只能含在住档案");
        assertTrue(actives.stream().anyMatch(r -> r.getId() == 1L), "应含 record1");
    }
}
