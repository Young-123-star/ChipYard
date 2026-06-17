package com.company.dms.module.fee;

import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.service.FeeBillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FeeBillTypeTest {

    @Autowired FeeBillService billService;

    @Test
    void rent_generate_sets_bill_type_1() {
        billService.generate("2026-07");
        FeeBill rent = billService.getByRecordAndPeriod(1L, "2026-07", 1);
        assertNotNull(rent, "应生成住宿费账单");
        assertEquals(1, rent.getBillType());
    }

    @Test
    void utility_bill_coexists_with_rent_same_record_period() {
        billService.generate("2026-07");
        Long eid = billService.createUtilityBill(1L, 1L, 2L, "2026-07", 2, new BigDecimal("30.00"), "电费 30度×1.00");
        Long wid = billService.createUtilityBill(1L, 1L, 2L, "2026-07", 3, new BigDecimal("40.00"), "水费 8吨×5.00");
        assertNotNull(billService.getByRecordAndPeriod(1L, "2026-07", 1));
        assertEquals(2, billService.getBill(eid).getBillType());
        assertEquals(3, billService.getBill(wid).getBillType());
        assertEquals(0, new BigDecimal("30.00").compareTo(billService.getBill(eid).getAmount()));
    }

    @Test
    void rent_generate_idempotent_ignores_utility() {
        billService.generate("2026-07");
        billService.createUtilityBill(1L, 1L, 2L, "2026-07", 2, new BigDecimal("30.00"), "电费");
        billService.generate("2026-07");
        assertNotNull(billService.getByRecordAndPeriod(1L, "2026-07", 1));
    }
}
