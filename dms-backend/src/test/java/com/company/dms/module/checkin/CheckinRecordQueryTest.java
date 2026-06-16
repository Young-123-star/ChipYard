package com.company.dms.module.checkin;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.service.CheckinService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CheckinRecordQueryTest {

    @Autowired CheckinService checkinService;

    @Test
    void find_active_record_by_resident() {
        // 种子：张三 resident1 有在住档案 record1(status=1)
        CheckinRecord r = checkinService.findActiveRecordByResident(1L);
        assertNotNull(r);
        assertEquals(1L, r.getId());
    }

    @Test
    void find_active_returns_null_when_none() {
        // resident3(王五) 无在住档案
        assertNull(checkinService.findActiveRecordByResident(3L));
    }

    @Test
    void mark_checked_out_sets_status_and_date() {
        checkinService.markCheckedOut(1L, LocalDate.of(2026, 7, 1));
        CheckinRecord r = checkinService.getRecord(1L);
        assertEquals(2, r.getStatus(), "应为已退宿");
        assertEquals(LocalDate.of(2026, 7, 1), r.getCheckoutDate());
    }

    @Test
    void get_missing_record_throws() {
        assertThrows(BizException.class, () -> checkinService.getRecord(999999L));
    }
}
