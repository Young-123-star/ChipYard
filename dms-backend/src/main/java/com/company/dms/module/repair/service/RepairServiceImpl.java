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
import com.company.dms.module.resource.mapper.BuildingMapper;
import com.company.dms.module.resource.mapper.RoomMapper;
import com.company.dms.module.resource.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RepairServiceImpl implements RepairService {

    private static final int ORDER_NO_MAX_ATTEMPTS = 3;

    private final RepairOrderMapper repairOrderMapper;
    private final RoomService roomService;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final ResidentService residentService;

    public RepairServiceImpl(RepairOrderMapper repairOrderMapper, RoomService roomService,
                             RoomMapper roomMapper, BuildingMapper buildingMapper,
                             ResidentService residentService) {
        this.repairOrderMapper = repairOrderMapper;
        this.roomService = roomService;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
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
        voPage.setRecords(toVOList(p.getRecords()));
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
        return toVOList(List.of(getOrder(id))).get(0);
    }

    @Override
    @Transactional
    public Long create(RepairCreateDTO dto) {
        Long roomId = resolveRoomId(dto);
        Long residentId = resolveResidentId(dto);
        RepairOrder order = new RepairOrder();
        BeanUtils.copyProperties(dto, order);
        order.setRoomId(roomId);
        order.setResidentId(residentId);
        order.setPriority(dto.getPriority() == null ? 1 : dto.getPriority());
        order.setStatus(1);
        // 工单号按“RO-yyyyMM-序号”生成，并发下可能撞唯一索引，冲突时重新生成单号重试
        for (int attempt = 1; ; attempt++) {
            order.setOrderNo(nextOrderNo());
            try {
                repairOrderMapper.insert(order);
                return order.getId();
            } catch (DuplicateKeyException e) {
                if (attempt >= ORDER_NO_MAX_ATTEMPTS) throw e;
            }
        }
    }

    private Long resolveRoomId(RepairCreateDTO dto) {
        if (dto.getRoomId() != null) return roomService.getById(dto.getRoomId()).getId();
        String code = trimToNull(dto.getRoomCode());
        if (code == null) throw new BizException("room is required");
        if (dto.getBuildingId() == null) throw new BizException("按房号报修时必须指定楼栋");
        return roomService.getByRoomNumber(dto.getBuildingId(), code).getId();
    }

    private Long resolveResidentId(RepairCreateDTO dto) {
        if (dto.getResidentId() != null) return residentService.getById(dto.getResidentId()).getId();
        String code = trimToNull(dto.getResidentCode());
        if (code == null) return null;
        Resident resident = residentService.getByEmployeeNo(code);
        if (resident != null) return resident.getId();
        if (code.chars().allMatch(Character::isDigit)) return residentService.getById(Long.valueOf(code)).getId();
        throw new BizException(ResultCode.NOT_FOUND.getCode(), "resident not found");
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
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
        refreshRoomIfNoOpenOrders(order.getRoomId());
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        RepairOrder order = getOrder(id);
        if (order.getStatus() != 1 && order.getStatus() != 2) throw new BizException("only pending or processing repair order can be canceled");
        order.setStatus(4);
        repairOrderMapper.updateById(order);
        refreshRoomIfNoOpenOrders(order.getRoomId());
    }

    private void refreshRoomIfNoOpenOrders(Long roomId) {
        Long open = repairOrderMapper.selectCount(Wrappers.<RepairOrder>lambdaQuery()
                .eq(RepairOrder::getRoomId, roomId)
                .in(RepairOrder::getStatus, 1, 2));
        if (open == 0) roomService.restoreStatusFromOccupancy(roomId);
    }

    private String nextOrderNo() {
        String ym = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        Long count = repairOrderMapper.selectCount(Wrappers.<RepairOrder>lambdaQuery()
                .likeRight(RepairOrder::getOrderNo, "RO-" + ym + "-"));
        return "RO-" + ym + "-" + String.format("%03d", count + 1);
    }

    /** 批量装配展示字段（房号/楼栋名/居住人姓名），避免逐行单查。 */
    private List<RepairOrderVO> toVOList(List<RepairOrder> orders) {
        if (orders.isEmpty()) return List.of();
        Set<Long> roomIds = orders.stream().map(RepairOrder::getRoomId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Room> roomMap = roomIds.isEmpty() ? Map.of()
                : roomMapper.selectBatchIds(roomIds).stream()
                        .collect(Collectors.toMap(Room::getId, room -> room));
        Set<Long> buildingIds = roomMap.values().stream().map(Room::getBuildingId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Building> buildingMap = buildingIds.isEmpty() ? Map.of()
                : buildingMapper.selectBatchIds(buildingIds).stream()
                        .collect(Collectors.toMap(Building::getId, building -> building));
        Set<Long> residentIds = orders.stream().map(RepairOrder::getResidentId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Resident> residentMap = residentIds.isEmpty() ? Map.of()
                : residentService.listByIds(residentIds).stream()
                        .collect(Collectors.toMap(Resident::getId, resident -> resident));
        return orders.stream().map(order -> toVO(order, roomMap, buildingMap, residentMap))
                .collect(Collectors.toList());
    }

    private RepairOrderVO toVO(RepairOrder order, Map<Long, Room> roomMap,
                               Map<Long, Building> buildingMap, Map<Long, Resident> residentMap) {
        RepairOrderVO vo = new RepairOrderVO();
        BeanUtils.copyProperties(order, vo);
        Room room = order.getRoomId() == null ? null : roomMap.get(order.getRoomId());
        if (room != null) {
            vo.setRoomNumber(room.getRoomNumber());
            Building building = room.getBuildingId() == null ? null : buildingMap.get(room.getBuildingId());
            if (building != null) vo.setBuildingName(building.getBuildingName());
        }
        Resident resident = order.getResidentId() == null ? null : residentMap.get(order.getResidentId());
        if (resident != null) vo.setResidentName(resident.getRealName());
        return vo;
    }
}
