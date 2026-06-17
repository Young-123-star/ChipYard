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
class CheckinActiveByRoomTest {

    @Autowired CheckinService checkinService;

    @Test
    void list_active_by_room_returns_room_occupants() {
        List<CheckinRecord> occ = checkinService.listActiveRecordsByRoom(2L);
        assertFalse(occ.isEmpty());
        assertTrue(occ.stream().allMatch(r -> r.getStatus() == 1 && r.getRoomId() == 2L));
    }

    @Test
    void list_active_by_room_empty_for_vacant_room() {
        assertTrue(checkinService.listActiveRecordsByRoom(3L).isEmpty());
    }
}
