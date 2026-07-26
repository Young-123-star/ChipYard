package com.company.dms.module.checkout.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.checkout.dto.CheckoutCreateDTO;
import com.company.dms.module.checkout.dto.CheckoutQuery;
import com.company.dms.module.checkout.dto.CreateCheckoutCommand;
import com.company.dms.module.checkout.entity.CheckoutOrder;
import com.company.dms.module.checkout.mapper.CheckoutOrderMapper;
import com.company.dms.module.checkout.vo.CheckoutOrderVO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final CheckoutOrderMapper orderMapper;
    private final ResidentService residentService;
    private final CheckinService checkinService;
    private final com.company.dms.module.resource.service.BedService bedService;
    private final com.company.dms.module.resource.service.RoomService roomService;
    private final com.company.dms.module.fee.service.FeeBillService feeBillService;

    public CheckoutServiceImpl(CheckoutOrderMapper orderMapper, ResidentService residentService, CheckinService checkinService,
                               com.company.dms.module.resource.service.BedService bedService,
                               com.company.dms.module.resource.service.RoomService roomService,
                               com.company.dms.module.fee.service.FeeBillService feeBillService) {
        this.orderMapper = orderMapper;
        this.residentService = residentService;
        this.checkinService = checkinService;
        this.bedService = bedService;
        this.roomService = roomService;
        this.feeBillService = feeBillService;
    }

    @Override
    public PageResult<CheckoutOrderVO> pageOrders(CheckoutQuery query) {
        Page<CheckoutOrder> p = orderMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<CheckoutOrder>lambdaQuery()
                        .eq(query.getStatus() != null, CheckoutOrder::getStatus, query.getStatus())
                        .eq(query.getSource() != null, CheckoutOrder::getSource, query.getSource())
                        .orderByDesc(CheckoutOrder::getId));
        List<Long> residentIds = p.getRecords().stream().map(CheckoutOrder::getResidentId).distinct().collect(Collectors.toList());
        Map<Long, Resident> residents = residentIds.isEmpty() ? Map.of()
                : residentService.listByIds(residentIds).stream()
                    .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));
        List<Long> recordIds = p.getRecords().stream().map(CheckoutOrder::getCheckinRecordId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, CheckinRecord> records = recordIds.isEmpty() ? Map.of()
                : checkinService.listRecordsByIds(recordIds).stream()
                    .collect(Collectors.toMap(CheckinRecord::getId, Function.identity(), (a, b) -> a));
        Page<CheckoutOrderVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(o -> toVO(o, residents.get(o.getResidentId()),
                o.getCheckinRecordId() == null ? null : records.get(o.getCheckinRecordId()))).collect(Collectors.toList()));
        return PageResult.of(voPage);
    }

    private CheckoutOrderVO toVO(CheckoutOrder o, Resident r, CheckinRecord rec) {
        CheckoutOrderVO vo = new CheckoutOrderVO();
        BeanUtils.copyProperties(o, vo);
        if (r != null) {
            vo.setResidentName(r.getRealName());
            vo.setEmployeeNo(r.getEmployeeNo());
        }
        if (rec != null) {
            vo.setRoomId(rec.getRoomId());
            vo.setBedId(rec.getBedId());
        }
        return vo;
    }

    @Override
    public CheckoutOrder getOrder(Long id) {
        CheckoutOrder o = orderMapper.selectById(id);
        if (o == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "退宿单不存在");
        return o;
    }

    @Override
    public Long createManual(CheckoutCreateDTO dto) {
        residentService.getById(dto.getResidentId()); // 校验居住人存在
        CheckinRecord active = checkinService.findActiveRecordByResident(dto.getResidentId());
        if (active == null) throw new BizException("该居住人无在住记录，无法发起退宿");
        CheckoutOrder pending = findPendingOrderByResident(dto.getResidentId());
        if (pending != null) return pending.getId(); // 幂等：已有待退宿单
        CheckoutOrder o = new CheckoutOrder();
        o.setBizNo("MANUAL-CO-" + dto.getResidentId() + "-" + System.nanoTime());
        o.setResidentId(dto.getResidentId());
        o.setCheckinRecordId(active.getId());
        o.setSource(3);
        o.setReason(dto.getReason());
        o.setExpectCheckoutDate(dto.getExpectCheckoutDate());
        o.setStatus(1);
        orderMapper.insert(o);
        return o.getId();
    }

    @Override
    public Long createOrderFromCommand(CreateCheckoutCommand cmd) {
        CheckoutOrder existing = orderMapper.selectOne(Wrappers.<CheckoutOrder>lambdaQuery()
                .eq(CheckoutOrder::getBizNo, cmd.getBizNo()).last("limit 1"));
        if (existing != null) return existing.getId(); // 幂等
        if (cmd.getResidentId() != null) {
            CheckoutOrder pending = findPendingOrderByResident(cmd.getResidentId());
            if (pending != null) return pending.getId(); // 幂等：已有待退宿单
        }
        CheckoutOrder o = new CheckoutOrder();
        BeanUtils.copyProperties(cmd, o);
        o.setStatus(1);
        try {
            orderMapper.insert(o);
        } catch (DuplicateKeyException e) {
            // 并发下唯一索引兜底：按 bizNo 回查返回已有记录
            CheckoutOrder existed = orderMapper.selectOne(Wrappers.<CheckoutOrder>lambdaQuery()
                    .eq(CheckoutOrder::getBizNo, cmd.getBizNo()).last("limit 1"));
            if (existed != null) return existed.getId();
            throw e;
        }
        return o.getId();
    }

    /** 查该居住人待退宿(status=1)的退宿单；无则返回 null。 */
    private CheckoutOrder findPendingOrderByResident(Long residentId) {
        return orderMapper.selectOne(Wrappers.<CheckoutOrder>lambdaQuery()
                .eq(CheckoutOrder::getResidentId, residentId)
                .eq(CheckoutOrder::getStatus, 1)
                .last("limit 1"));
    }

    @Override
    public void cancel(Long orderId) {
        getOrder(orderId); // 存在性校验
        int affected = orderMapper.update(null, Wrappers.<CheckoutOrder>lambdaUpdate()
                .set(CheckoutOrder::getStatus, 3)
                .eq(CheckoutOrder::getId, orderId)
                .eq(CheckoutOrder::getStatus, 1));
        if (affected == 0) throw new BizException("单据状态已变化，请刷新后重试");
    }

    @Override
    @Transactional
    public void confirm(Long orderId, LocalDate checkoutDate) {
        CheckoutOrder order = getOrder(orderId);
        if (order.getStatus() != 1) throw new BizException("仅待退宿的单可办理退宿");
        if (order.getCheckinRecordId() == null) throw new BizException("该退宿单无关联在住档案，无法办理");

        LocalDate date = checkoutDate != null ? checkoutDate : LocalDate.now();
        CheckinRecord record = checkinService.getRecord(order.getCheckinRecordId());
        if (record.getStatus() != 1) throw new BizException("关联入住档案非在住状态");
        if (date.isBefore(record.getCheckinDate())) throw new BizException("退宿日期不能早于入住日期");

        // 释放床位 + 刷新房间统计
        bedService.release(record.getBedId());
        roomService.refreshOccupancy(record.getRoomId());

        // 档案归档
        checkinService.markCheckedOut(record.getId(), date);

        // 欠费结算挂账：未缴账单 → 挂账，回填离场欠费金额
        order.setArrearsAmount(feeBillService.settleArrearsForRecord(record.getId()));

        // 退宿单完成（条件更新，防并发重复办理）
        int affected = orderMapper.update(null, Wrappers.<CheckoutOrder>lambdaUpdate()
                .set(CheckoutOrder::getStatus, 2)
                .set(CheckoutOrder::getArrearsAmount, order.getArrearsAmount())
                .eq(CheckoutOrder::getId, order.getId())
                .eq(CheckoutOrder::getStatus, 1));
        if (affected == 0) throw new BizException("单据状态已变化，请刷新后重试");
    }
}
