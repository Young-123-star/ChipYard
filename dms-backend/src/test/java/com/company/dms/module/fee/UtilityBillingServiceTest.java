package com.company.dms.module.fee;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.module.fee.dto.MeterReadingDTO;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.entity.UtilitySettlement;
import com.company.dms.module.fee.mapper.FeeBillMapper;
import com.company.dms.module.fee.mapper.UtilitySettlementMapper;
import com.company.dms.module.fee.service.UtilityBillingService;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.RoomMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UtilityBillingServiceTest {
    @Autowired UtilityBillingService service;
    @Autowired RoomMapper roomMapper;
    @Autowired FeeBillMapper billMapper;
    @Autowired UtilitySettlementMapper settlementMapper;

    @Test
    void room_allowance_generates_employee_and_company_shares_and_can_void() {
        roomMapper.delete(Wrappers.<Room>lambdaQuery().ne(Room::getId, 2L));
        Room room = roomMapper.selectById(2L);
        room.setSettlementMode(2);
        room.setUtilityAccountCode("A102");
        room.setElectricityRule(2);
        room.setWaterRule(0);
        roomMapper.updateById(room);

        MeterReadingDTO reading = new MeterReadingDTO();
        reading.setBuildingId(1L);
        reading.setAccountCode("A102");
        reading.setTargetType(2);
        reading.setRoomId(2L);
        reading.setPeriod("2026-07");
        reading.setMeterType(1);
        reading.setPrevReading(BigDecimal.ZERO);
        reading.setCurrentReading(new BigDecimal("300"));
        service.saveReading(reading);

        Map<String, Object> preview = service.preview("2026-07");
        assertEquals(Boolean.TRUE, preview.get("valid"));
        @SuppressWarnings("unchecked")
        Map<String, Object> account = ((List<Map<String, Object>>) preview.get("accounts")).get(0);
        assertEquals(0, new BigDecimal("26.92").compareTo((BigDecimal) account.get("employeeAmount")));
        assertEquals(0, new BigDecimal("134.57").compareTo((BigDecimal) account.get("companyAmount")));

        service.generate("2026-07");
        UtilitySettlement settlement = settlementMapper.selectOne(Wrappers.<UtilitySettlement>lambdaQuery()
                .eq(UtilitySettlement::getPeriod, "2026-07").eq(UtilitySettlement::getStatus, 1));
        assertNotNull(settlement);
        FeeBill bill = billMapper.selectOne(Wrappers.<FeeBill>lambdaQuery()
                .isNotNull(FeeBill::getUtilityResultId).eq(FeeBill::getPeriod, "2026-07"));
        assertEquals(0, new BigDecimal("26.92").compareTo(bill.getAmount()));

        service.voidSettlement(settlement.getId());
        assertEquals(2, settlementMapper.selectById(settlement.getId()).getStatus());
        assertEquals(3, billMapper.selectById(bill.getId()).getStatus());
    }
}
