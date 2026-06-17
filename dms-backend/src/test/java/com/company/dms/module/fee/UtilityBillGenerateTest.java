package com.company.dms.module.fee;

import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.mapper.CheckinRecordMapper;
import com.company.dms.module.fee.dto.MeterReadingDTO;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.mapper.FeeBillMapper;
import com.company.dms.module.fee.service.MeterService;
import com.company.dms.module.fee.vo.GenerateResultVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UtilityBillGenerateTest {

    @Autowired MeterService meterService;
    @Autowired CheckinRecordMapper recordMapper;
    @Autowired FeeBillMapper billMapper;

    private CheckinRecord active(long resident, long room, long bed) {
        CheckinRecord r = new CheckinRecord();
        r.setIntakeId(1L); r.setBuildingId(1L); r.setFloorId(2L);
        r.setResidentId(resident); r.setRoomId(room); r.setBedId(bed);
        r.setCheckinDate(LocalDate.of(2026, 1, 1)); r.setStatus(1);
        recordMapper.insert(r);
        return r;
    }

    @Test
    void single_occupant_room_gets_full_amount() {
        GenerateResultVO res = meterService.generateUtilityBills("2026-06");
        assertTrue(res.getGenerated() >= 2, "电+水各一张");
        List<FeeBill> bills = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getCheckinRecordId, 1L).eq(FeeBill::getPeriod, "2026-06").in(FeeBill::getBillType, 2, 3));
        assertEquals(2, bills.size());
        BigDecimal e = bills.stream().filter(b -> b.getBillType() == 2).findFirst().get().getAmount();
        BigDecimal w = bills.stream().filter(b -> b.getBillType() == 3).findFirst().get().getAmount();
        assertEquals(0, new BigDecimal("30.00").compareTo(e));
        assertEquals(0, new BigDecimal("40.00").compareTo(w));
    }

    @Test
    void splits_equally_with_remainder_to_first() {
        active(2L, 3L, 5L);
        active(3L, 3L, 6L);
        MeterReadingDTO dto = new MeterReadingDTO();
        dto.setRoomId(3L); dto.setPeriod("2026-09"); dto.setMeterType(1);
        dto.setCurrentReading(new BigDecimal("25.01"));
        meterService.saveReading(dto);

        meterService.generateUtilityBills("2026-09");
        List<FeeBill> bills = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getRoomId, 3L).eq(FeeBill::getPeriod, "2026-09").eq(FeeBill::getBillType, 2)
                .orderByAsc(FeeBill::getId));
        assertEquals(2, bills.size(), "2 人各一张");
        BigDecimal sum = bills.get(0).getAmount().add(bills.get(1).getAmount());
        assertEquals(0, new BigDecimal("25.01").compareTo(sum), "合计=房间总额");
        assertEquals(0, new BigDecimal("12.50").compareTo(bills.get(0).getAmount()));
        assertEquals(0, new BigDecimal("12.51").compareTo(bills.get(1).getAmount()));
    }

    @Test
    void idempotent_skips_already_generated() {
        meterService.generateUtilityBills("2026-06");
        GenerateResultVO again = meterService.generateUtilityBills("2026-06");
        assertEquals(0, again.getGenerated(), "重复生成不再新增");
    }

    @Test
    void skips_room_with_no_active_occupant() {
        MeterReadingDTO dto = new MeterReadingDTO();
        dto.setRoomId(3L); dto.setPeriod("2026-10"); dto.setMeterType(1);
        dto.setCurrentReading(new BigDecimal("5.00"));
        meterService.saveReading(dto);
        GenerateResultVO res = meterService.generateUtilityBills("2026-10");
        assertEquals(0, res.getGenerated());
        assertTrue(res.getSkipped() >= 1);
    }
}
