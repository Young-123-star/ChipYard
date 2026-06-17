package com.company.dms.module.fee;

import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.mapper.FeeBillMapper;
import com.company.dms.module.fee.service.ReportService;
import com.company.dms.module.fee.vo.PeriodSummaryVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportServiceTest {

    @Autowired ReportService reportService;
    @Autowired FeeBillMapper billMapper;

    private int seq = 0;

    /** 插入一张账单到指定账期/类型/状态。billNo 用自增序号保证唯一。 */
    private void bill(String period, int billType, String amount, int status, long residentId) {
        FeeBill b = new FeeBill();
        b.setBillNo("RPT-" + (++seq));
        b.setCheckinRecordId(1L);
        b.setResidentId(residentId);
        b.setRoomId(2L);
        b.setPeriod(period);
        b.setBillType(billType);
        b.setAmount(new BigDecimal(amount));
        b.setStatus(status);
        billMapper.insert(b);
    }

    private PeriodSummaryVO period(String period) {
        return reportService.getPeriodSummary().stream()
                .filter(v -> v.getPeriod().equals(period)).findFirst().orElseThrow();
    }

    @Test
    void period_summary_splits_types_and_computes_rate() {
        bill("2099-01", 1, "800.00", 2, 1L);  // 住宿 已缴
        bill("2099-01", 2, "30.00", 1, 1L);   // 电 未缴
        bill("2099-01", 3, "40.00", 4, 1L);   // 水 挂账
        bill("2099-01", 1, "100.00", 3, 1L);  // 作废 → 不计

        PeriodSummaryVO vo = period("2099-01");
        assertEquals(0, new BigDecimal("800.00").compareTo(vo.getRentTotal()));
        assertEquals(0, new BigDecimal("30.00").compareTo(vo.getElecTotal()));
        assertEquals(0, new BigDecimal("40.00").compareTo(vo.getWaterTotal()));
        assertEquals(0, new BigDecimal("870.00").compareTo(vo.getTotal()), "作废不计应收");
        assertEquals(0, new BigDecimal("800.00").compareTo(vo.getPaid()));
        assertEquals(0, new BigDecimal("70.00").compareTo(vo.getUnpaid()), "未缴含挂账");
        assertEquals(0, new BigDecimal("91.95").compareTo(vo.getCollectRate()), "800*100/870=91.95");
    }

    @Test
    void period_summary_zero_total_rate_is_zero() {
        bill("2099-03", 1, "0.00", 1, 1L);
        assertEquals(0, BigDecimal.ZERO.compareTo(period("2099-03").getCollectRate()));
    }

    @Test
    void period_summary_sorted_ascending() {
        bill("2099-02", 1, "10.00", 1, 1L);
        bill("2099-01", 1, "10.00", 1, 1L);
        List<PeriodSummaryVO> list = reportService.getPeriodSummary();
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getPeriod().compareTo(list.get(i).getPeriod()) <= 0,
                    "period 升序");
        }
    }

    private com.company.dms.module.fee.vo.BuildingSummaryVO building(long buildingId) {
        return reportService.getBuildingSummary().stream()
                .filter(v -> v.getBuildingId().equals(buildingId)).findFirst().orElseThrow();
    }

    @Test
    void building_summary_aggregates_and_resolves_name() {
        // 种子已有 room2(building1) 一张未缴 800；用 delta 断言避免种子干扰
        com.company.dms.module.fee.vo.BuildingSummaryVO base = building(1L);
        BigDecimal paid0 = base.getPaid();
        BigDecimal unpaid0 = base.getUnpaid();

        bill("2099-05", 1, "500.00", 2, 1L);  // room2→building1 已缴
        bill("2099-05", 2, "60.00", 1, 1L);   // room2→building1 未缴
        bill("2099-05", 1, "999.00", 3, 1L);  // 作废 → 不计

        com.company.dms.module.fee.vo.BuildingSummaryVO vo = building(1L);
        assertEquals("A栋员工宿舍", vo.getBuildingName());
        assertEquals(0, paid0.add(new BigDecimal("500.00")).compareTo(vo.getPaid()));
        assertEquals(0, unpaid0.add(new BigDecimal("60.00")).compareTo(vo.getUnpaid()));
        assertEquals(0, vo.getPaid().add(vo.getUnpaid()).compareTo(vo.getTotal()),
                "应收=已缴+未缴（作废已排除）");
    }
}
