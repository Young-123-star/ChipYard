package com.company.dms.module.resource;

import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.BedService;
import com.company.dms.module.resource.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OccupancyServiceTest {

    @Autowired BedService bedService;
    @Autowired RoomService roomService;

    @Test
    void occupy_bed_then_refresh_room_occupancy() {
        // 种子房间1(A101) 有 2 张空床(bed id 1,2)，occupied_beds=0，status=1空闲
        bedService.occupy(1L, 2L); // 让居住人2入住 bed 1
        Bed bed = bedService.getById(1L);
        assertEquals(2, bed.getStatus(), "床位应为已入住");
        assertEquals(2L, bed.getCurrentUserId());

        roomService.refreshOccupancy(1L);
        Room room = roomService.getById(1L);
        assertEquals(1, room.getOccupiedBeds(), "房间已占 1 床");
        assertEquals(1, room.getStatus(), "未满仍为空闲");
    }
}
