package com.company.dms.module.fee;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.service.FeeBillService;
import com.company.dms.module.fee.vo.GenerateResultVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FeeBillServiceTest {

    @Autowired FeeBillService billService;

    @Test
    void generate_creates_bill_for_active_record_with_price_snapshot() {
        // 张三 record1 在住，room2=room_type2，标准 800.00；账期 2026-07 尚无账单
        GenerateResultVO r = billService.generate("2026-07");
        assertEquals(1, r.getGenerated(), "应为张三生成 1 张");
        FeeBill bill = billService.getByRecordAndPeriod(1L, "2026-07", 1);
        assertNotNull(bill);
        assertEquals(0, new BigDecimal("800.00").compareTo(bill.getAmount()), "金额=房型月单价快照");
        assertEquals(1, bill.getStatus(), "新账单未缴");
    }

    @Test
    void generate_is_idempotent_for_same_period() {
        billService.generate("2026-07");
        GenerateResultVO again = billService.generate("2026-07");
        assertEquals(0, again.getGenerated(), "重复生成不再新增");
        assertTrue(again.getSkipped() >= 1, "已存在的被跳过");
    }

    @Test
    void generate_for_seeded_period_skips_existing() {
        // 种子已有 2026-06 张三账单 → 该账期重复生成 0 新增
        GenerateResultVO r = billService.generate("2026-06");
        assertEquals(0, r.getGenerated());
    }

    @Test
    void pay_marks_paid_then_rejects_repay() {
        // 种子账单 id=1 (2026-06, 未缴)
        billService.pay(1L, 1);
        FeeBill b = billService.getBill(1L);
        assertEquals(2, b.getStatus(), "已缴");
        assertEquals(1, b.getPayMethod());
        assertNotNull(b.getPaidAt());
        assertThrows(BizException.class, () -> billService.pay(1L, 1), "已缴不可再缴");
    }

    @Test
    void void_marks_voided_then_rejects() {
        billService.voidBill(1L);
        assertEquals(3, billService.getBill(1L).getStatus());
        assertThrows(BizException.class, () -> billService.voidBill(1L), "已作废不可再作废");
    }
}
