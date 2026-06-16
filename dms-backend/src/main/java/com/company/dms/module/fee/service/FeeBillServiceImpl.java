package com.company.dms.module.fee.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.fee.dto.BillQuery;
import com.company.dms.module.fee.entity.FeeBill;
import com.company.dms.module.fee.entity.FeeStandard;
import com.company.dms.module.fee.mapper.FeeBillMapper;
import com.company.dms.module.fee.vo.FeeBillVO;
import com.company.dms.module.fee.vo.GenerateResultVO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeeBillServiceImpl implements FeeBillService {

    private final FeeBillMapper billMapper;
    private final FeeStandardService standardService;
    private final CheckinService checkinService;
    private final ResidentService residentService;
    private final RoomService roomService;

    public FeeBillServiceImpl(FeeBillMapper billMapper, FeeStandardService standardService,
                              CheckinService checkinService, ResidentService residentService, RoomService roomService) {
        this.billMapper = billMapper;
        this.standardService = standardService;
        this.checkinService = checkinService;
        this.residentService = residentService;
        this.roomService = roomService;
    }

    @Override
    public PageResult<FeeBillVO> pageBills(BillQuery query) {
        Page<FeeBill> p = billMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<FeeBill>lambdaQuery()
                        .eq(query.getPeriod() != null && !query.getPeriod().isBlank(), FeeBill::getPeriod, query.getPeriod())
                        .eq(query.getStatus() != null, FeeBill::getStatus, query.getStatus())
                        .eq(query.getResidentId() != null, FeeBill::getResidentId, query.getResidentId())
                        .orderByDesc(FeeBill::getId));
        Page<FeeBillVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return PageResult.of(voPage);
    }

    private FeeBillVO toVO(FeeBill b) {
        FeeBillVO vo = new FeeBillVO();
        BeanUtils.copyProperties(b, vo);
        try {
            Resident r = residentService.getById(b.getResidentId());
            vo.setResidentName(r.getRealName());
            vo.setEmployeeNo(r.getEmployeeNo());
        } catch (Exception ignore) { /* 居住人不存在则留空 */ }
        if (b.getRoomId() != null) {
            try {
                Room room = roomService.getById(b.getRoomId());
                vo.setRoomNumber(room.getRoomNumber());
                vo.setRoomType(room.getRoomType());
            } catch (Exception ignore) { /* 房间不存在则留空 */ }
        }
        return vo;
    }

    @Override
    public FeeBill getBill(Long id) {
        FeeBill b = billMapper.selectById(id);
        if (b == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "账单不存在");
        return b;
    }

    @Override
    public FeeBill getByRecordAndPeriod(Long checkinRecordId, String period) {
        return billMapper.selectOne(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getCheckinRecordId, checkinRecordId)
                .eq(FeeBill::getPeriod, period)
                .last("limit 1"));
    }

    @Override
    @Transactional
    public GenerateResultVO generate(String period) {
        List<CheckinRecord> actives = checkinService.listActiveRecords();
        int generated = 0, skipped = 0;
        for (CheckinRecord rec : actives) {
            Room room = roomService.getById(rec.getRoomId());
            FeeStandard std = standardService.findByRoomType(room.getRoomType());
            if (std == null) { skipped++; continue; }                 // 无收费标准
            if (getByRecordAndPeriod(rec.getId(), period) != null) { skipped++; continue; } // 幂等
            FeeBill bill = new FeeBill();
            bill.setBillNo("BILL-" + rec.getId() + "-" + period.replace("-", ""));
            bill.setCheckinRecordId(rec.getId());
            bill.setResidentId(rec.getResidentId());
            bill.setRoomId(rec.getRoomId());
            bill.setPeriod(period);
            bill.setAmount(std.getMonthlyPrice());
            bill.setStatus(1);
            billMapper.insert(bill);
            generated++;
        }
        return GenerateResultVO.of(generated, skipped);
    }

    @Override
    public void pay(Long id, Integer payMethod) {
        FeeBill b = getBill(id);
        if (b.getStatus() != 1 && b.getStatus() != 4) throw new BizException("仅未缴或挂账账单可缴费");
        b.setStatus(2);
        b.setPaidAt(LocalDateTime.now());
        b.setPayMethod(payMethod);
        billMapper.updateById(b);
    }

    @Override
    public void voidBill(Long id) {
        FeeBill b = getBill(id);
        if (b.getStatus() != 1) throw new BizException("仅未缴账单可作废");
        b.setStatus(3);
        billMapper.updateById(b);
    }

    @Override
    public java.util.List<FeeBill> listUnpaidByRecord(Long checkinRecordId) {
        return billMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getCheckinRecordId, checkinRecordId)
                .eq(FeeBill::getStatus, 1));
    }

    @Override
    public java.math.BigDecimal settleArrearsForRecord(Long checkinRecordId) {
        java.util.List<FeeBill> unpaid = listUnpaidByRecord(checkinRecordId);
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (FeeBill b : unpaid) {
            total = total.add(b.getAmount());
            b.setStatus(4);
            billMapper.updateById(b);
        }
        return total;
    }
}
