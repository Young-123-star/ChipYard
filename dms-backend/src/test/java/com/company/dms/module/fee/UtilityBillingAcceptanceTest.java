package com.company.dms.module.fee;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.mapper.CheckinRecordMapper;
import com.company.dms.module.fee.dto.MeterReadingDTO;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.entity.UtilityRate;
import com.company.dms.module.fee.entity.UtilityRoomResult;
import com.company.dms.module.fee.entity.UtilitySettlement;
import com.company.dms.module.fee.mapper.FeeBillMapper;
import com.company.dms.module.fee.mapper.MeterReadingMapper;
import com.company.dms.module.fee.mapper.UtilityRateMapper;
import com.company.dms.module.fee.mapper.UtilityRoomResultMapper;
import com.company.dms.module.fee.mapper.UtilitySettlementMapper;
import com.company.dms.module.fee.service.UtilityBillingService;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.RoomMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UtilityBillingAcceptanceTest {
    private static final String PERIOD = "2026-08";

    @Autowired UtilityBillingService service;
    @Autowired RoomMapper roomMapper;
    @Autowired CheckinRecordMapper recordMapper;
    @Autowired MeterReadingMapper readingMapper;
    @Autowired UtilitySettlementMapper settlementMapper;
    @Autowired UtilityRoomResultMapper resultMapper;
    @Autowired FeeBillMapper billMapper;
    @Autowired UtilityRateMapper rateMapper;

    @BeforeEach
    void resetFixture() {
        clearFixture();
        UtilityRate rate = rateMapper.selectById(1L);
        rate.setElectricityPrice(new BigDecimal("0.5383"));
        rate.setWaterPrice(new BigDecimal("4.1500"));
        rateMapper.updateById(rate);
    }

    private void clearFixture() {
        billMapper.delete(null);
        resultMapper.delete(null);
        settlementMapper.delete(null);
        readingMapper.delete(null);
        recordMapper.delete(null);
        roomMapper.delete(null);
    }

    @Test
    void generates_the_august_golden_matrix_and_conserves_every_cent() {
        Map<String, Room> rooms = goldenFixture();

        Map<String, Object> preview = service.preview(PERIOD);
        assertEquals(Boolean.TRUE, preview.get("valid"));
        Map<String, Map<String, Object>> accounts = accounts(preview);
        assertAccount(accounts.get("HH"), "625.81", "149.16", "476.65");
        assertAccount(accounts.get("BOUNDARY"), "205.13", "0.00", "205.13");
        assertAccount(accounts.get("EXCESS"), "244.49", "39.37", "205.12");
        assertAccount(accounts.get("COUPLE"), "95.33", "95.33", "0.00");
        assertAccount(accounts.get("SUPERVISOR"), "222.96", "17.83", "205.13");
        assertAccount(accounts.get("COMPANY"), "95.33", "0.00", "95.33");
        assertAccount(accounts.get("EMPTY"), "244.49", "0.00", "244.49");

        Map<String, Object> generated = service.generate(PERIOD);
        assertEquals(7, generated.get("settlements"));
        assertEquals(14, generated.get("bills"));

        List<UtilitySettlement> settlements = service.listSettlements(PERIOD);
        assertEquals(7, settlements.size());
        assertMoney("1733.54", sum(settlements, UtilitySettlement::getTotalCost));
        assertMoney("301.69", sum(settlements, UtilitySettlement::getEmployeeAmount));
        assertMoney("1431.85", sum(settlements, UtilitySettlement::getCompanyAmount));
        assertEquals(14, billMapper.selectCount(Wrappers.<FeeBill>lambdaQuery().eq(FeeBill::getPeriod, PERIOD)));

        for (UtilitySettlement settlement : settlements) {
            List<UtilityRoomResult> results = resultMapper.selectList(Wrappers.<UtilityRoomResult>lambdaQuery()
                    .eq(UtilityRoomResult::getSettlementId, settlement.getId()));
            assertMoney(settlement.getTotalCost(), sum(results, UtilityRoomResult::getTotalCost));
            assertMoney(settlement.getEmployeeAmount(), sum(results, UtilityRoomResult::getEmployeeAmount));
            assertMoney(settlement.getCompanyAmount(), sum(results, UtilityRoomResult::getCompanyAmount));
            assertMoney(settlement.getTotalCost(), settlement.getEmployeeAmount().add(settlement.getCompanyAmount()));
            for (UtilityRoomResult result : results) {
                List<FeeBill> resultBills = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                        .eq(FeeBill::getUtilityResultId, result.getId()));
                assertMoney(result.getEmployeeAmount(), sum(resultBills, FeeBill::getAmount));
            }
        }

        List<UtilityRoomResult> householdElectricity = resultMapper.selectList(Wrappers.<UtilityRoomResult>lambdaQuery()
                .eq(UtilityRoomResult::getSettlementId, settlement(settlements, "HH").getId())
                .eq(UtilityRoomResult::getUtilityType, 1));
        assertMoney("310.0000", householdElectricity.stream().filter(r -> r.getRoomId().equals(rooms.get("hh1").getId()))
                .findFirst().orElseThrow().getActualUsage());
        assertMoney("390.0000", householdElectricity.stream().filter(r -> r.getRoomId().equals(rooms.get("hh2").getId()))
                .findFirst().orElseThrow().getActualUsage());

        List<FeeBill> coupleBills = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getRoomId, rooms.get("couple").getId()).eq(FeeBill::getPeriod, PERIOD)
                .orderByAsc(FeeBill::getBillType).orderByAsc(FeeBill::getId));
        assertEquals(4, coupleBills.size());
        assertMoney("26.92", coupleBills.get(0).getAmount());
        assertMoney("26.91", coupleBills.get(1).getAmount());
        assertMoney("20.75", coupleBills.get(2).getAmount());
        assertMoney("20.75", coupleBills.get(3).getAmount());

        List<FeeBill> householdWaterBills = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getRoomId, rooms.get("hh1").getId())
                .eq(FeeBill::getPeriod, PERIOD)
                .eq(FeeBill::getBillType, 3)
                .orderByAsc(FeeBill::getId));
        assertEquals(2, householdWaterBills.size());
        assertMoney("10.38", householdWaterBills.get(0).getAmount());
        assertMoney("10.37", householdWaterBills.get(1).getAmount());
    }

    @Test
    void rejects_missing_readings_and_a_household_master_below_its_submeters() {
        Room room = room("single", "SINGLE", 2, 2, 2);
        Map<String, Object> missingPreview = service.preview(PERIOD);
        assertFalse((Boolean) missingPreview.get("valid"));
        assertTrue(((List<?>) missingPreview.get("errors")).stream()
                .map(String::valueOf).anyMatch(error -> error.contains("missing meter reading")));
        assertThrows(BizException.class, () -> service.generate(PERIOD));

        clearFixture();
        Room first = room("101", "HH", 1, 1, 1);
        Room second = room("102", "HH", 1, 1, 1);
        reading("HH", 1, first, 1, "500");
        reading("HH", 2, first, 1, "260");
        reading("HH", 2, second, 1, "340");
        assertTrue(((List<?>) service.preview(PERIOD).get("errors")).stream()
                .map(String::valueOf).anyMatch(error -> error.contains("common usage is negative")));
    }

    @Test
    void locks_readings_after_generation_and_allows_unpaid_void_then_regeneration_but_not_paid_void() {
        Room room = singleAccountFixture("EXCESS", 2, 2);
        record(room, 100L, LocalDate.of(2026, 8, 1), null);
        reading("EXCESS", 2, room, 1, "300");
        reading("EXCESS", 2, room, 2, "20");
        reading("EXCESS", 2, room, 3, "0");
        service.generate(PERIOD);

        assertThrows(BizException.class, () -> service.generate(PERIOD));
        assertThrows(BizException.class, () -> reading("EXCESS", 2, room, 1, "301"));

        UtilitySettlement original = settlement(service.listSettlements(PERIOD), "EXCESS");
        service.voidSettlement(original.getId());
        assertEquals(2, settlementMapper.selectById(original.getId()).getStatus());
        assertTrue(billMapper.selectList(Wrappers.<FeeBill>lambdaQuery().eq(FeeBill::getPeriod, PERIOD))
                .stream().allMatch(bill -> bill.getStatus() == 3));
        assertEquals(1, service.generate(PERIOD).get("settlements"));

        UtilitySettlement regenerated = service.listSettlements(PERIOD).stream()
                .filter(item -> item.getAccountCode().equals("EXCESS"))
                .filter(item -> item.getStatus() == 1)
                .filter(item -> !item.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        FeeBill paid = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getPeriod, PERIOD)
                .eq(FeeBill::getStatus, 1)
                .orderByAsc(FeeBill::getId)).get(0);
        paid.setStatus(2);
        billMapper.updateById(paid);
        assertThrows(BizException.class, () -> service.voidSettlement(regenerated.getId()));
    }

    @Test
    void includes_cutoff_day_24_but_excludes_day_25_and_checkout_on_day_24() {
        Room room = singleAccountFixture("CUTOFF", 2, 2);
        record(room, 101L, LocalDate.of(2026, 8, 24), null);
        record(room, 102L, LocalDate.of(2026, 8, 25), null);
        record(room, 103L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 8, 24));
        reading("CUTOFF", 2, room, 1, "300");
        reading("CUTOFF", 2, room, 2, "20");
        reading("CUTOFF", 2, room, 3, "0");

        service.generate(PERIOD);
        List<FeeBill> bills = billMapper.selectList(Wrappers.<FeeBill>lambdaQuery().eq(FeeBill::getPeriod, PERIOD));
        assertEquals(2, bills.size());
        assertTrue(bills.stream().allMatch(bill -> bill.getCheckinRecordId().equals(
                recordMapper.selectOne(Wrappers.<CheckinRecord>lambdaQuery().eq(CheckinRecord::getResidentId, 101L)).getId())));
    }

    private Map<String, Room> goldenFixture() {
        Room hh1 = room("101", "HH", 1, 1, 1);
        Room hh2 = room("102", "HH", 1, 1, 1);
        Room boundary = singleAccountFixture("BOUNDARY", 2, 2);
        Room excess = singleAccountFixture("EXCESS", 2, 2);
        Room couple = singleAccountFixture("COUPLE", 3, 3);
        Room supervisor = singleAccountFixture("SUPERVISOR", 2, 2);
        Room company = singleAccountFixture("COMPANY", 4, 4);
        Room empty = singleAccountFixture("EMPTY", 2, 2);

        record(hh1, 1L, LocalDate.of(2026, 1, 1), null);
        record(hh1, 2L, LocalDate.of(2026, 1, 1), null);
        record(hh2, 3L, LocalDate.of(2026, 1, 1), null);
        record(boundary, 4L, LocalDate.of(2026, 1, 1), null);
        record(excess, 5L, LocalDate.of(2026, 1, 1), null);
        record(couple, 6L, LocalDate.of(2026, 1, 1), null);
        record(couple, 7L, LocalDate.of(2026, 1, 1), null);
        record(supervisor, 8L, LocalDate.of(2026, 1, 1), null);
        record(company, 9L, LocalDate.of(2026, 1, 1), null);

        reading("HH", 1, hh1, 1, "700");
        reading("HH", 2, hh1, 1, "260");
        reading("HH", 2, hh2, 1, "340");
        reading("HH", 1, hh1, 2, "40");
        reading("HH", 1, hh1, 3, "20");
        readings("BOUNDARY", boundary, "250", "17");
        readings("EXCESS", excess, "300", "20");
        readings("COUPLE", couple, "100", "10");
        readings("SUPERVISOR", supervisor, "260", "20");
        readings("COMPANY", company, "100", "10");
        readings("EMPTY", empty, "300", "20");
        return Map.of("hh1", hh1, "hh2", hh2, "couple", couple);
    }

    private Room singleAccountFixture(String account, int electricityRule, int waterRule) {
        return room(account, account, 2, electricityRule, waterRule);
    }

    private Room room(String roomNumber, String account, int mode, int electricityRule, int waterRule) {
        Room room = new Room();
        room.setBuildingId(1L);
        room.setFloorId(1L);
        room.setRoomNumber(roomNumber);
        room.setRoomType(2);
        room.setBedCount(2);
        room.setOccupiedBeds(0);
        room.setStatus(1);
        room.setSettlementMode(mode);
        room.setUtilityAccountCode(account);
        room.setElectricityRule(electricityRule);
        room.setWaterRule(waterRule);
        roomMapper.insert(room);
        return room;
    }

    private void readings(String account, Room room, String electricity, String water) {
        reading(account, 2, room, 1, electricity);
        reading(account, 2, room, 2, water);
        reading(account, 2, room, 3, "0");
    }

    private void reading(String account, int targetType, Room room, int meterType, String current) {
        MeterReadingDTO dto = new MeterReadingDTO();
        dto.setBuildingId(1L);
        dto.setAccountCode(account);
        dto.setTargetType(targetType);
        dto.setRoomId(room.getId());
        dto.setPeriod(PERIOD);
        dto.setMeterType(meterType);
        dto.setPrevReading(BigDecimal.ZERO);
        dto.setCurrentReading(new BigDecimal(current));
        service.saveReading(dto);
    }

    private void record(Room room, long residentId, LocalDate checkin, LocalDate checkout) {
        CheckinRecord record = new CheckinRecord();
        record.setIntakeId(residentId);
        record.setResidentId(residentId);
        record.setBuildingId(1L);
        record.setFloorId(1L);
        record.setRoomId(room.getId());
        record.setBedId(residentId);
        record.setCheckinDate(checkin);
        record.setCheckoutDate(checkout);
        record.setStatus(checkout == null ? 1 : 2);
        recordMapper.insert(record);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Object>> accounts(Map<String, Object> preview) {
        return ((List<Map<String, Object>>) preview.get("accounts")).stream()
                .collect(Collectors.toMap(account -> (String) account.get("accountCode"), Function.identity()));
    }

    private UtilitySettlement settlement(List<UtilitySettlement> settlements, String account) {
        return settlements.stream().filter(item -> item.getAccountCode().equals(account)).findFirst().orElseThrow();
    }

    private <T> BigDecimal sum(List<T> values, Function<T, BigDecimal> amount) {
        return values.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assertAccount(Map<String, Object> account, String total, String employee, String company) {
        assertNotNull(account);
        assertMoney(total, (BigDecimal) account.get("totalCost"));
        assertMoney(employee, (BigDecimal) account.get("employeeAmount"));
        assertMoney(company, (BigDecimal) account.get("companyAmount"));
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertMoney(new BigDecimal(expected), actual);
    }

    private void assertMoney(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), () -> "expected " + expected + " but was " + actual);
    }
}
