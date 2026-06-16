package com.company.dms.module.fee;

import com.company.dms.common.exception.BizException;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.service.FeeBillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FeeArrearsServiceTest {

    @Autowired FeeBillService billService;

    @Test
    void list_unpaid_by_record_returns_only_unpaid() {
        List<FeeBill> unpaid = billService.listUnpaidByRecord(1L);
        assertFalse(unpaid.isEmpty());
        assertTrue(unpaid.stream().allMatch(b -> b.getStatus() == 1));
    }

    @Test
    void settle_arrears_marks_held_and_returns_total() {
        BigDecimal total = billService.settleArrearsForRecord(1L);
        assertEquals(0, new BigDecimal("800.00").compareTo(total), "欠费总额=未缴账单合计");
        FeeBill b = billService.getBill(1L);
        assertEquals(4, b.getStatus(), "应为挂账");
        assertEquals(0, BigDecimal.ZERO.compareTo(billService.settleArrearsForRecord(1L)));
    }

    @Test
    void settle_returns_zero_when_no_unpaid() {
        assertEquals(0, BigDecimal.ZERO.compareTo(billService.settleArrearsForRecord(999L)));
    }

    @Test
    void pay_allows_held_bill() {
        billService.settleArrearsForRecord(1L);   // 账单1 → 挂账
        billService.pay(1L, 2);                    // 挂账可缴费
        assertEquals(2, billService.getBill(1L).getStatus(), "挂账缴费后→已缴");
    }

    @Test
    void pay_still_rejects_paid_bill() {
        billService.pay(1L, 1);                    // 未缴→已缴
        assertThrows(BizException.class, () -> billService.pay(1L, 1), "已缴不可再缴");
    }
}
