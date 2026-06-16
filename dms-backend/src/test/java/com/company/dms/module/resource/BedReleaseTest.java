package com.company.dms.module.resource;

import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.service.BedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BedReleaseTest {

    @Autowired BedService bedService;

    @Test
    void release_clears_current_user_id() {
        // 种子 bed3 已入住(status=2, current_user_id=1)
        bedService.release(3L);
        Bed b = bedService.getById(3L);
        assertEquals(1, b.getStatus(), "床位应空闲");
        assertNull(b.getCurrentUserId(), "current_user_id 应持久化为 null");
    }
}
