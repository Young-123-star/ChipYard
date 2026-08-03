package com.company.dms.module.fee;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.module.fee.dto.MeterReadingDTO;
import com.company.dms.module.fee.dto.UtilityAccountSaveDTO;
import com.company.dms.module.fee.dto.UtilityBatchApplyDTO;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.entity.UtilityRate;
import com.company.dms.module.fee.entity.UtilityRoomResult;
import com.company.dms.module.fee.entity.UtilitySettlement;
import com.company.dms.module.fee.mapper.FeeBillMapper;
import com.company.dms.module.fee.mapper.UtilityRateMapper;
import com.company.dms.module.fee.mapper.UtilityRoomResultMapper;
import com.company.dms.module.fee.mapper.UtilitySettlementMapper;
import com.company.dms.module.fee.service.UtilityBillingService;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.RoomMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Autowired UtilityRoomResultMapper resultMapper;
    @Autowired UtilityRateMapper rateMapper;

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
        UtilityRoomResult result = resultMapper.selectOne(Wrappers.<UtilityRoomResult>lambdaQuery()
                .eq(UtilityRoomResult::getSettlementId, settlement.getId()));
        assertNotNull(result);
        assertEquals(result.getId(), bill.getUtilityResultId(), "账单应回填分摊结果 id");

        service.voidSettlement(settlement.getId());
        assertEquals(2, settlementMapper.selectById(settlement.getId()).getStatus());
        assertEquals(3, billMapper.selectById(bill.getId()).getStatus());
    }

    @Test
    void configurable_electric_allowance_changes_employee_share() {
        roomMapper.delete(Wrappers.<Room>lambdaQuery().ne(Room::getId, 2L));
        Room room = roomMapper.selectById(2L);
        room.setSettlementMode(2);
        room.setUtilityAccountCode("A102");
        room.setElectricityRule(2);
        room.setWaterRule(0);
        roomMapper.updateById(room);
        // 电免额 250 → 200，超补贴部分相应增加
        UtilityRate rate = rateMapper.selectById(1L);
        rate.setElectricAllowance(new BigDecimal("200"));
        rateMapper.updateById(rate);

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
        // (300 - 200) × 0.5383 = 53.83
        assertEquals(0, new BigDecimal("53.83").compareTo((BigDecimal) account.get("employeeAmount")));

        service.generate("2026-07");
        UtilitySettlement settlement = settlementMapper.selectOne(Wrappers.<UtilitySettlement>lambdaQuery()
                .eq(UtilitySettlement::getPeriod, "2026-07").eq(UtilitySettlement::getStatus, 1));
        UtilityRoomResult result = resultMapper.selectOne(Wrappers.<UtilityRoomResult>lambdaQuery()
                .eq(UtilityRoomResult::getSettlementId, settlement.getId()));
        assertTrue(result.getCalculationNote().contains("200 度"), "免额文案应使用配置值");
    }

    @Test
    void configurable_cycle_days_shift_settlement_window() {
        roomMapper.delete(Wrappers.<Room>lambdaQuery().ne(Room::getId, 2L));
        Room room = roomMapper.selectById(2L);
        room.setSettlementMode(2);
        room.setUtilityAccountCode("A102");
        room.setElectricityRule(2);
        room.setWaterRule(0);
        roomMapper.updateById(room);
        // 周期改为上月 21 日 ~ 本月 20 日
        UtilityRate rate = rateMapper.selectById(1L);
        rate.setCycleStartDay(21);
        rate.setCycleEndDay(20);
        rateMapper.updateById(rate);

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

        service.generate("2026-07");
        UtilitySettlement settlement = settlementMapper.selectOne(Wrappers.<UtilitySettlement>lambdaQuery()
                .eq(UtilitySettlement::getPeriod, "2026-07").eq(UtilitySettlement::getStatus, 1));
        assertEquals(LocalDate.of(2026, 6, 21), settlement.getCycleStart());
        assertEquals(LocalDate.of(2026, 7, 20), settlement.getCycleEnd());
    }

    @Test
    void save_account_batch_updates_rooms_and_rejects_invalid_config() {
        roomMapper.delete(Wrappers.<Room>lambdaQuery().notIn(Room::getId, 1L, 2L));

        // 户级账户批量配置两个房间
        UtilityAccountSaveDTO dto = new UtilityAccountSaveDTO();
        dto.setBuildingId(1L);
        dto.setAccountCode("HH");
        dto.setSettlementMode(1);
        dto.setElectricityRule(1);
        dto.setWaterRule(1);
        dto.setRoomIds(List.of(1L, 2L));
        assertEquals(2, service.saveAccount(dto));
        Room first = roomMapper.selectById(1L);
        assertEquals("HH", first.getUtilityAccountCode());
        assertEquals(1, first.getSettlementMode());
        assertEquals(1, first.getElectricityRule());

        // 模式2留空账户编号时默认用房间号，每个房间独立成户
        UtilityAccountSaveDTO perRoom = new UtilityAccountSaveDTO();
        perRoom.setBuildingId(1L);
        perRoom.setSettlementMode(2);
        perRoom.setElectricityRule(2);
        perRoom.setWaterRule(2);
        perRoom.setRoomIds(List.of(1L, 2L));
        assertEquals(2, service.saveAccount(perRoom));
        assertEquals("A101", roomMapper.selectById(1L).getUtilityAccountCode());
        assertEquals("A102", roomMapper.selectById(2L).getUtilityAccountCode());

        // 规则1必须户级模式
        UtilityAccountSaveDTO badMode = new UtilityAccountSaveDTO();
        badMode.setBuildingId(1L);
        badMode.setAccountCode("X");
        badMode.setSettlementMode(2);
        badMode.setElectricityRule(1);
        badMode.setWaterRule(0);
        badMode.setRoomIds(List.of(1L));
        assertThrows(BizException.class, () -> service.saveAccount(badMode));

        // 双规则不能都为0
        UtilityAccountSaveDTO noRule = new UtilityAccountSaveDTO();
        noRule.setBuildingId(1L);
        noRule.setAccountCode("Y");
        noRule.setSettlementMode(2);
        noRule.setElectricityRule(0);
        noRule.setWaterRule(0);
        noRule.setRoomIds(List.of(1L));
        assertThrows(BizException.class, () -> service.saveAccount(noRule));

        // 房间账户只能包含一个房间
        UtilityAccountSaveDTO multiRoom = new UtilityAccountSaveDTO();
        multiRoom.setBuildingId(1L);
        multiRoom.setAccountCode("Z");
        multiRoom.setSettlementMode(2);
        multiRoom.setElectricityRule(2);
        multiRoom.setWaterRule(0);
        multiRoom.setRoomIds(List.of(1L, 2L));
        assertThrows(BizException.class, () -> service.saveAccount(multiRoom));
    }

    @Test
    void batch_apply_room_mode_updates_all_matched_rooms() {
        // 整栋楼房型2刷成房间账户+规则2/2
        UtilityBatchApplyDTO dto = new UtilityBatchApplyDTO();
        dto.setBuildingId(1L);
        dto.setRoomType(2);
        dto.setSettlementMode(2);
        dto.setElectricityRule(2);
        dto.setWaterRule(2);
        Map<String, Object> result = service.batchApply(dto);
        assertEquals(3, result.get("updated"));
        assertEquals(0, result.get("skipped"));

        Room room = roomMapper.selectById(1L);
        assertEquals(2, room.getSettlementMode());
        assertEquals("A101", room.getUtilityAccountCode());
        assertEquals(2, room.getElectricityRule());
        assertEquals(2, room.getWaterRule());
        assertEquals("A202", roomMapper.selectById(4L).getUtilityAccountCode());
        // 房型3的 A201 不在过滤范围，保持未配置
        assertNull(roomMapper.selectById(3L).getSettlementMode());
    }

    @Test
    void batch_apply_household_mode_merges_coded_rooms_and_skips_uncoded() {
        // 只有 A101/A102 已有账户编码
        for (Long roomId : List.of(1L, 2L)) {
            Room room = roomMapper.selectById(roomId);
            room.setSettlementMode(1);
            room.setUtilityAccountCode("HH");
            roomMapper.updateById(room);
        }
        UtilityBatchApplyDTO dto = new UtilityBatchApplyDTO();
        dto.setBuildingId(1L);
        dto.setSettlementMode(1);
        dto.setElectricityRule(1);
        dto.setWaterRule(1);
        Map<String, Object> result = service.batchApply(dto);
        assertEquals(2, result.get("updated"));
        assertEquals(2, result.get("skipped"));

        Room merged = roomMapper.selectById(2L);
        assertEquals("HH", merged.getUtilityAccountCode());
        assertEquals(1, merged.getSettlementMode());
        assertEquals(1, merged.getElectricityRule());
        assertEquals(1, merged.getWaterRule());
        // 无编码房间跳过，四字段不被触碰
        Room skipped = roomMapper.selectById(4L);
        assertNull(skipped.getSettlementMode());
        assertNull(skipped.getUtilityAccountCode());
    }

    @Test
    void batch_apply_rejects_invalid_rule_combinations() {
        // 双规则都为0
        UtilityBatchApplyDTO bothZero = new UtilityBatchApplyDTO();
        bothZero.setSettlementMode(2);
        bothZero.setElectricityRule(0);
        bothZero.setWaterRule(0);
        assertThrows(BizException.class, () -> service.batchApply(bothZero));

        // 户级分摊规则必须按户结算模式
        UtilityBatchApplyDTO householdRule = new UtilityBatchApplyDTO();
        householdRule.setSettlementMode(2);
        householdRule.setElectricityRule(1);
        householdRule.setWaterRule(0);
        assertThrows(BizException.class, () -> service.batchApply(householdRule));

        // 夫妻房规则必须单房间账户
        UtilityBatchApplyDTO coupleRule = new UtilityBatchApplyDTO();
        coupleRule.setSettlementMode(1);
        coupleRule.setElectricityRule(3);
        coupleRule.setWaterRule(0);
        assertThrows(BizException.class, () -> service.batchApply(coupleRule));
    }
}
