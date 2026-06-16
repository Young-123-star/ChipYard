package com.company.dms.module.fee;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.fee.dto.FeeStandardDTO;
import com.company.dms.module.fee.entity.FeeStandard;
import com.company.dms.module.fee.service.FeeStandardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FeeStandardServiceTest {

    @Autowired FeeStandardService standardService;

    @Test
    void list_returns_seeded_standards() {
        List<FeeStandard> list = standardService.list();
        assertTrue(list.size() >= 3, "种子至少 3 条");
    }

    @Test
    void create_rejects_duplicate_room_type() {
        FeeStandardDTO dto = new FeeStandardDTO();
        dto.setRoomType(2); // 种子已有 room_type=2
        dto.setMonthlyPrice(new BigDecimal("999.00"));
        assertThrows(BizException.class, () -> standardService.create(dto));
    }

    @Test
    void create_then_update_then_delete() {
        FeeStandardDTO dto = new FeeStandardDTO();
        dto.setRoomType(6); // 种子未占用
        dto.setMonthlyPrice(new BigDecimal("300.00"));
        dto.setRemark("其他");
        Long id = standardService.create(dto);
        assertNotNull(id);

        FeeStandardDTO upd = new FeeStandardDTO();
        upd.setRoomType(6);
        upd.setMonthlyPrice(new BigDecimal("350.00"));
        standardService.update(id, upd);
        assertEquals(0, new BigDecimal("350.00").compareTo(standardService.getById(id).getMonthlyPrice()));

        standardService.delete(id);
        assertThrows(BizException.class, () -> standardService.getById(id));
    }
}
