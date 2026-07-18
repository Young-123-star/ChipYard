package com.company.dms.module.fee;

import com.company.dms.module.fee.dto.MeterReadingDTO;
import com.company.dms.module.fee.entity.MeterReading;
import com.company.dms.module.fee.entity.UtilityRate;
import com.company.dms.module.fee.service.MeterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MeterServiceTest {

    @Autowired MeterService meterService;

    @Test
    void get_rate_returns_seed() {
        UtilityRate rate = meterService.getRate();
        assertEquals(0, new BigDecimal("0.5383").compareTo(rate.getElectricityPrice()));
        assertEquals(0, new BigDecimal("4.1500").compareTo(rate.getWaterPrice()));
    }

    @Test
    void save_reading_auto_computes_from_prev() {
        MeterReadingDTO dto = new MeterReadingDTO();
        dto.setRoomId(2L); dto.setPeriod("2026-07"); dto.setMeterType(1);
        dto.setCurrentReading(new BigDecimal("160.00"));
        Long id = meterService.saveReading(dto);
        MeterReading r = meterService.getReading(id);
        assertEquals(0, new BigDecimal("130.00").compareTo(r.getPrevReading()), "上期取 2026-06 的 130");
        assertEquals(0, new BigDecimal("30.00").compareTo(r.getConsumption()));
        assertEquals(0, new BigDecimal("16.15").compareTo(r.getAmount()));
    }

    @Test
    void save_reading_is_idempotent_per_room_period_type() {
        MeterReadingDTO dto = new MeterReadingDTO();
        dto.setRoomId(2L); dto.setPeriod("2026-08"); dto.setMeterType(1);
        dto.setCurrentReading(new BigDecimal("200.00"));
        Long id1 = meterService.saveReading(dto);
        dto.setCurrentReading(new BigDecimal("210.00"));
        Long id2 = meterService.saveReading(dto);
        assertEquals(id1, id2, "同键更新不新增");
        assertEquals(0, new BigDecimal("210.00").compareTo(meterService.getReading(id1).getCurrentReading()));
    }

    @Test
    void save_first_reading_prev_zero() {
        MeterReadingDTO dto = new MeterReadingDTO();
        dto.setRoomId(3L); dto.setPeriod("2026-07"); dto.setMeterType(2);
        dto.setCurrentReading(new BigDecimal("12.00"));
        Long id = meterService.saveReading(dto);
        MeterReading r = meterService.getReading(id);
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getPrevReading()));
        assertEquals(0, new BigDecimal("12.00").compareTo(r.getConsumption()));
        assertEquals(0, new BigDecimal("49.80").compareTo(r.getAmount()), "12 x water price 4.1500");
    }
}
