package com.company.dms.module.checkout;

import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.checkout.service.CheckoutService;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.BedService;
import com.company.dms.module.resource.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CheckoutConfirmServiceTest {

    @Autowired CheckoutService checkoutService;
    @Autowired CheckinService checkinService;
    @Autowired BedService bedService;
    @Autowired RoomService roomService;

    @Test
    void confirm_releases_bed_and_archives_record() {
        // 种子 SEED-CO-1 (order id=1) 待退宿，关联 record1(张三, bed3, room2, 在住)
        checkoutService.confirm(1L, LocalDate.of(2026, 7, 1));

        assertEquals(2, checkoutService.getOrder(1L).getStatus());
        assertEquals(2, checkinService.getRecord(1L).getStatus());
        assertEquals(LocalDate.of(2026, 7, 1), checkinService.getRecord(1L).getCheckoutDate());
        Bed bed = bedService.getById(3L);
        assertEquals(1, bed.getStatus());
        assertNull(bed.getCurrentUserId());
        Room room = roomService.getById(2L);
        assertEquals(0, room.getOccupiedBeds());
    }

    @Test
    void confirm_rejected_when_order_not_pending() {
        checkoutService.cancel(1L); // 先取消 → 状态3
        assertThrows(RuntimeException.class, () -> checkoutService.confirm(1L, LocalDate.now()));
    }
}
