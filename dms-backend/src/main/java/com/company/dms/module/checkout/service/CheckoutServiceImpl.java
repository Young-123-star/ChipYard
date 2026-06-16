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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final CheckoutOrderMapper orderMapper;
    private final ResidentService residentService;
    private final CheckinService checkinService;

    public CheckoutServiceImpl(CheckoutOrderMapper orderMapper, ResidentService residentService, CheckinService checkinService) {
        this.orderMapper = orderMapper;
        this.residentService = residentService;
        this.checkinService = checkinService;
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
                : residentService.page(allResidentsQuery()).getRecords().stream()
                    .filter(r -> residentIds.contains(r.getId()))
                    .collect(Collectors.toMap(Resident::getId, Function.identity(), (a, b) -> a));
        Page<CheckoutOrderVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(o -> toVO(o, residents.get(o.getResidentId()))).collect(Collectors.toList()));
        return PageResult.of(voPage);
    }

    private com.company.dms.module.resident.dto.ResidentQuery allResidentsQuery() {
        com.company.dms.module.resident.dto.ResidentQuery q = new com.company.dms.module.resident.dto.ResidentQuery();
        q.setSize(1000);
        return q;
    }

    private CheckoutOrderVO toVO(CheckoutOrder o, Resident r) {
        CheckoutOrderVO vo = new CheckoutOrderVO();
        BeanUtils.copyProperties(o, vo);
        if (r != null) {
            vo.setResidentName(r.getRealName());
            vo.setEmployeeNo(r.getEmployeeNo());
        }
        if (o.getCheckinRecordId() != null) {
            try {
                CheckinRecord rec = checkinService.getRecord(o.getCheckinRecordId());
                vo.setRoomId(rec.getRoomId());
                vo.setBedId(rec.getBedId());
            } catch (Exception ignore) { /* 档案不存在则留空 */ }
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
        CheckoutOrder o = new CheckoutOrder();
        BeanUtils.copyProperties(cmd, o);
        o.setStatus(1);
        orderMapper.insert(o);
        return o.getId();
    }

    @Override
    public void cancel(Long orderId) {
        CheckoutOrder o = getOrder(orderId);
        if (o.getStatus() != 1) throw new BizException("仅待退宿的单可取消");
        o.setStatus(3);
        orderMapper.updateById(o);
    }

    @Override
    @Transactional
    public void confirm(Long orderId, LocalDate checkoutDate) {
        // 在 Task 7 实现
        throw new UnsupportedOperationException("confirm 在 Task 7 实现");
    }
}
