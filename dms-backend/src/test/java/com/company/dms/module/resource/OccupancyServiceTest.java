package com.company.dms.module.resource;

import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.BedService;
import com.company.dms.module.resource.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OccupancyServiceTest {

    @Autowired BedService bedService;
    @Autowired RoomService roomService;

    @Test
    void occupy_bed_then_refresh_room_occupancy() {
        bedService.occupy(1L, 2L);
        Bed bed = bedService.getById(1L);
        assertEquals(2, bed.getStatus(), "bed should be occupied");
        assertEquals(2L, bed.getCurrentUserId());

        roomService.refreshOccupancy(1L);
        Room room = roomService.getById(1L);
        assertEquals(1, room.getOccupiedBeds(), "room should have one occupied bed");
        assertEquals(1, room.getStatus(), "partially occupied room stays available");
    }
}
