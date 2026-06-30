package com.company.dms.module.repair.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.repair.dto.RepairAcceptDTO;
import com.company.dms.module.repair.dto.RepairCompleteDTO;
import com.company.dms.module.repair.dto.RepairCreateDTO;
import com.company.dms.module.repair.dto.RepairQuery;
import com.company.dms.module.repair.entity.RepairOrder;
import com.company.dms.module.repair.mapper.RepairOrderMapper;
import com.company.dms.module.repair.vo.RepairOrderVO;
import com.company.dms.module.resident.entity.Resident;
import com.company.dms.module.resident.service.ResidentService;
import com.company.dms.module.resource.entity.Building;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.service.BuildingService;
import com.company.dms.module.resource.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class RepairServiceImpl implements RepairService {

    private final RepairOrderMapper repairOrderMapper;
    private final RoomService roomService;
    private final BuildingService buildingService;
    private final ResidentService residentService;

    public RepairServiceImpl(RepairOrderMapper repairOrderMapper, RoomService roomService,
                             BuildingService buildingService, ResidentService residentService) {
        this.repairOrderMapper = repairOrderMapper;
        this.roomService = roomService;
        this.buildingService = buildingService;
        this.residentService = residentService;
    }

    @Override
    public PageResult<RepairOrderVO> pageOrders(RepairQuery query) {
        Page<RepairOrder> p = repairOrderMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<RepairOrder>lambdaQuery()
                        .eq(query.getStatus() != null, RepairOrder::getStatus, query.getStatus())
                        .eq(query.getPriority() != null, RepairOrder::getPriority, query.getPriority())
                        .eq(query.getRoomId() != null, RepairOrder::getRoomId, query.getRoomId())
                        .orderByDesc(RepairOrder::getId));
        Page<RepairOrderVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return PageResult.of(voPage);
    }

    @Override
    public RepairOrder getOrder(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "repair order not found");
        return order;
    }

    @Override
    public RepairOrderVO getDetail(Long id) {
        return toVO(getOrder(id));
    }

    @Override
    public Long create(RepairCreateDTO dto) {
        roomService.getById(dto.getRoomId());
        if (dto.getResidentId() != null) residentService.getById(dto.getResidentId());
        RepairOrder order = new RepairOrder();
        BeanUtils.copyProperties(dto, order);
        order.setOrderNo(nextOrderNo());
        order.setPriority(dto.getPriority() == null ? 1 : dto.getPriority());
        order.setStatus(1);
        repairOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    @Transactional
    public void accept(Long id, RepairAcceptDTO dto) {
        RepairOrder order = getOrder(id);
        if (order.getStatus() != 1) throw new BizException("only pending repair order can be accepted");
        order.setStatus(2);
        order.setHandler(dto.getHandler());
        order.setAcceptedAt(LocalDateTime.now());
        repairOrderMapper.updateById(order);
        roomService.updateStatus(order.getRoomId(), 3);
    }

    @Override
    @Transactional
    public void complete(Long id, RepairCompleteDTO dto) {
        RepairOrder order = getOrder(id);
        if (order.getStatus() != 2) throw new BizException("only processing repair order can be completed");
        order.setStatus(3);
        order.setResult(dto.getResult());
        order.setCompletedAt(LocalDateTime.now());
        repairOrderMapper.updateById(order);
        refreshRoomIfNoProcessingOrders(order.getRoomId());
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        RepairOrder order = getOrder(id);
        if (order.getStatus() != 1 && order.getStatus() != 2) throw new BizException("only pending or processing repair order can be canceled");
        boolean wasProcessing = order.getStatus() == 2;
        order.setStatus(4);
        repairOrderMapper.updateById(order);
        if (wasProcessing) refreshRoomIfNoProcessingOrders(order.getRoomId());
    }

    private void refreshRoomIfNoProcessingOrders(Long roomId) {
        Long processing = repairOrderMapper.selectCount(Wrappers.<RepairOrder>lambdaQuery()
                .eq(RepairOrder::getRoomId, roomId)
                .eq(RepairOrder::getStatus, 2));
        if (processing == 0) roomService.restoreStatusFromOccupancy(roomId);
    }

    private String nextOrderNo() {
        String ym = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Long count = repairOrderMapper.selectCount(Wrappers.<RepairOrder>lambdaQuery()
                .likeRight(RepairOrder::getOrderNo, "RO-" + ym + "-"));
        return "RO-" + ym + "-" + String.format("%03d", count + 1);
    }

    private RepairOrderVO toVO(RepairOrder order) {
        RepairOrderVO vo = new RepairOrderVO();
        BeanUtils.copyProperties(order, vo);
        try {
            Room room = roomService.getById(order.getRoomId());
            vo.setRoomNumber(room.getRoomNumber());
            Building building = buildingService.getById(room.getBuildingId());
            vo.setBuildingName(building.getBuildingName());
        } catch (Exception ignore) { /* display fields are best effort */ }
        if (order.getResidentId() != null) {
            try {
                Resident resident = residentService.getById(order.getResidentId());
                vo.setResidentName(resident.getRealName());
            } catch (Exception ignore) { /* display fields are best effort */ }
        }
        return vo;
    }
}
