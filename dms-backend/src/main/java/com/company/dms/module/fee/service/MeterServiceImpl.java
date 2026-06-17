package com.company.dms.module.fee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.fee.dto.MeterQuery;
import com.company.dms.module.fee.dto.MeterReadingDTO;
import com.company.dms.module.fee.dto.UtilityRateDTO;
import com.company.dms.module.fee.entity.MeterReading;
import com.company.dms.module.fee.entity.UtilityRate;
import com.company.dms.module.fee.mapper.MeterReadingMapper;
import com.company.dms.module.fee.mapper.UtilityRateMapper;
import com.company.dms.module.fee.vo.GenerateResultVO;
import com.company.dms.module.fee.vo.MeterReadingVO;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

@Service
public class MeterServiceImpl implements MeterService {

    private final MeterReadingMapper readingMapper;
    private final UtilityRateMapper rateMapper;
    private final RoomService roomService;

    public MeterServiceImpl(MeterReadingMapper readingMapper, UtilityRateMapper rateMapper, RoomService roomService) {
        this.readingMapper = readingMapper;
        this.rateMapper = rateMapper;
        this.roomService = roomService;
    }

    @Override
    public UtilityRate getRate() {
        UtilityRate rate = rateMapper.selectById(1L);
        if (rate == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "水电单价未配置");
        return rate;
    }

    @Override
    public void updateRate(UtilityRateDTO dto) {
        UtilityRate rate = getRate();
        rate.setElectricityPrice(dto.getElectricityPrice());
        rate.setWaterPrice(dto.getWaterPrice());
        rateMapper.updateById(rate);
    }

    @Override
    public PageResult<MeterReadingVO> pageReadings(MeterQuery query) {
        Page<MeterReading> p = readingMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<MeterReading>lambdaQuery()
                        .eq(query.getPeriod() != null && !query.getPeriod().isBlank(), MeterReading::getPeriod, query.getPeriod())
                        .eq(query.getRoomId() != null, MeterReading::getRoomId, query.getRoomId())
                        .eq(query.getMeterType() != null, MeterReading::getMeterType, query.getMeterType())
                        .orderByDesc(MeterReading::getId));
        Page<MeterReadingVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return PageResult.of(voPage);
    }

    private MeterReadingVO toVO(MeterReading m) {
        MeterReadingVO vo = new MeterReadingVO();
        BeanUtils.copyProperties(m, vo);
        try {
            Room room = roomService.getById(m.getRoomId());
            vo.setRoomNumber(room.getRoomNumber());
        } catch (Exception ignore) { /* 房间不存在则留空 */ }
        return vo;
    }

    @Override
    public MeterReading getReading(Long id) {
        MeterReading r = readingMapper.selectById(id);
        if (r == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "抄表记录不存在");
        return r;
    }

    @Override
    public Long saveReading(MeterReadingDTO dto) {
        UtilityRate rate = getRate();
        BigDecimal price = dto.getMeterType() == 1 ? rate.getElectricityPrice() : rate.getWaterPrice();
        MeterReading prev = readingMapper.selectOne(Wrappers.<MeterReading>lambdaQuery()
                .eq(MeterReading::getRoomId, dto.getRoomId())
                .eq(MeterReading::getMeterType, dto.getMeterType())
                .lt(MeterReading::getPeriod, dto.getPeriod())
                .orderByDesc(MeterReading::getPeriod).last("limit 1"));
        BigDecimal prevReading = prev != null ? prev.getCurrentReading() : BigDecimal.ZERO;
        BigDecimal consumption = dto.getCurrentReading().subtract(prevReading);
        BigDecimal amount = consumption.multiply(price).setScale(2, RoundingMode.HALF_UP);

        MeterReading existing = readingMapper.selectOne(Wrappers.<MeterReading>lambdaQuery()
                .eq(MeterReading::getRoomId, dto.getRoomId())
                .eq(MeterReading::getPeriod, dto.getPeriod())
                .eq(MeterReading::getMeterType, dto.getMeterType())
                .last("limit 1"));
        MeterReading r = existing != null ? existing : new MeterReading();
        r.setRoomId(dto.getRoomId());
        r.setPeriod(dto.getPeriod());
        r.setMeterType(dto.getMeterType());
        r.setPrevReading(prevReading);
        r.setCurrentReading(dto.getCurrentReading());
        r.setConsumption(consumption);
        r.setUnitPrice(price);
        r.setAmount(amount);
        if (existing != null) readingMapper.updateById(r); else readingMapper.insert(r);
        return r.getId();
    }

    @Override
    public GenerateResultVO generateUtilityBills(String period) {
        throw new UnsupportedOperationException("在 Task 6 实现");
    }
}
