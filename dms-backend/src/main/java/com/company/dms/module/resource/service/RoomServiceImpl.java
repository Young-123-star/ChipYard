package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.dict.service.DictService;
import com.company.dms.module.resource.dto.BoardQuery;
import com.company.dms.module.resource.dto.RoomQuery;
import com.company.dms.module.resource.dto.RoomSaveDTO;
import com.company.dms.module.resource.entity.Bed;
import com.company.dms.module.resource.entity.Floor;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.BedMapper;
import com.company.dms.module.resource.mapper.FloorMapper;
import com.company.dms.module.resource.mapper.RoomMapper;
import com.company.dms.module.resource.vo.RoomBoardVO;
import com.company.dms.module.resource.vo.RoomSummaryVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final FloorMapper floorMapper;
    private final BedMapper bedMapper;
    private final DictService dictService;
    private final ObjectMapper objectMapper;
    private static final Map<String, String> LEGACY_FACILITY_NAMES = Map.of(
            "air_conditioner", "空调",
            "water_heater", "热水器",
            "wardrobe", "衣柜",
            "desk", "书桌"
    );

    public RoomServiceImpl(RoomMapper roomMapper, FloorMapper floorMapper,
                           BedMapper bedMapper,
                           DictService dictService,
                           ObjectMapper objectMapper) {
        this.roomMapper = roomMapper;
        this.floorMapper = floorMapper;
        this.bedMapper = bedMapper;
        this.dictService = dictService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<Room> page(RoomQuery query) {
        Page<Room> p = roomMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<Room>lambdaQuery()
                        .eq(query.getBuildingId() != null, Room::getBuildingId, query.getBuildingId())
                        .eq(query.getFloorId() != null, Room::getFloorId, query.getFloorId())
                        .eq(query.getRoomType() != null, Room::getRoomType, query.getRoomType())
                        .eq(query.getStatus() != null, Room::getStatus, query.getStatus())
                        .orderByAsc(Room::getRoomNumber));
        return PageResult.of(p);
    }

    @Override
    public RoomSummaryVO summary(RoomQuery query) {
        List<Room> rooms = roomMapper.selectList(Wrappers.<Room>lambdaQuery()
                .eq(query.getBuildingId() != null, Room::getBuildingId, query.getBuildingId())
                .eq(query.getFloorId() != null, Room::getFloorId, query.getFloorId())
                .eq(query.getRoomType() != null, Room::getRoomType, query.getRoomType())
                .eq(query.getStatus() != null, Room::getStatus, query.getStatus()));
        RoomSummaryVO vo = new RoomSummaryVO();
        vo.setTotal(rooms.size());
        vo.setTotalBeds(rooms.stream().mapToInt(r -> r.getBedCount() == null ? 0 : r.getBedCount()).sum());
        vo.setOccupiedBeds(rooms.stream().mapToInt(r -> r.getOccupiedBeds() == null ? 0 : r.getOccupiedBeds()).sum());
        vo.setFreeBeds(vo.getTotalBeds() - vo.getOccupiedBeds());
        return vo;
    }

    @Override
    public List<RoomBoardVO> board(BoardQuery query) {
        List<Room> rooms = roomMapper.selectList(Wrappers.<Room>lambdaQuery()
                .eq(query.getBuildingId() != null, Room::getBuildingId, query.getBuildingId())
                .eq(query.getFloorId() != null, Room::getFloorId, query.getFloorId())
                .orderByAsc(Room::getRoomNumber));
        if (rooms.isEmpty()) return List.of();

        List<Long> floorIds = rooms.stream().map(Room::getFloorId).distinct().collect(Collectors.toList());
        Map<Long, Integer> floorNumbers = floorMapper.selectBatchIds(floorIds).stream()
                .collect(Collectors.toMap(Floor::getId, Floor::getFloorNumber, (a, b) -> a));

        return rooms.stream()
                .map(r -> {
                    RoomBoardVO vo = new RoomBoardVO();
                    BeanUtils.copyProperties(r, vo);
                    vo.setFloorNumber(floorNumbers.get(r.getFloorId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Room getById(Long id) {
        Room r = roomMapper.selectById(id);
        if (r == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "房间不存在");
        return r;
    }

    @Override
    public Room getByRoomNumber(Long buildingId, String roomNumber) {
        Room r = roomMapper.selectOne(Wrappers.<Room>lambdaQuery()
                .eq(Room::getBuildingId, buildingId)
                .eq(Room::getRoomNumber, roomNumber).last("limit 1"));
        if (r == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "房间不存在");
        return r;
    }

    @Override
    public Long create(RoomSaveDTO dto) {
        normalizeUtilityConfig(dto);
        validateFloor(dto);
        Long count = roomMapper.selectCount(Wrappers.<Room>lambdaQuery()
                .eq(Room::getBuildingId, dto.getBuildingId())
                .eq(Room::getRoomNumber, dto.getRoomNumber()));
        if (count > 0) throw new BizException("该楼栋已存在相同房间号");
        Room r = new Room();
        BeanUtils.copyProperties(dto, r);
        r.setFacilities(normalizeFacilities(dto.getFacilities()));
        r.setOccupiedBeds(0);
        roomMapper.insert(r);
        return r.getId();
    }

    @Override
    public void update(Long id, RoomSaveDTO dto) {
        getById(id);
        normalizeUtilityConfig(dto);
        validateFloor(dto);
        Long count = roomMapper.selectCount(Wrappers.<Room>lambdaQuery()
                .eq(Room::getBuildingId, dto.getBuildingId())
                .eq(Room::getRoomNumber, dto.getRoomNumber())
                .ne(Room::getId, id));
        if (count > 0) throw new BizException("该楼栋已存在相同房间号");
        Room r = new Room();
        BeanUtils.copyProperties(dto, r);
        r.setId(id);
        r.setFacilities(normalizeFacilities(dto.getFacilities()));
        roomMapper.updateById(r);
    }

    @Override
    public void delete(Long id) {
        Room room = getById(id);
        if (room.getOccupiedBeds() != null && room.getOccupiedBeds() > 0) {
            throw new BizException("房间存在在住床位，不能删除");
        }
        Long bedCount = bedMapper.selectCount(Wrappers.<Bed>lambdaQuery().eq(Bed::getRoomId, id));
        if (bedCount > 0) throw new BizException("房间下存在床位，不能删除");
        roomMapper.deleteById(id);
    }

    @Override
    public void refreshOccupancy(Long roomId) {
        Room room = getById(roomId);
        long occupied = bedMapper.selectCount(Wrappers.<Bed>lambdaQuery()
                .eq(Bed::getRoomId, roomId)
                .eq(Bed::getStatus, 2));
        room.setOccupiedBeds((int) occupied);
        int bedCount = room.getBedCount() == null ? 0 : room.getBedCount();
        if (room.getStatus() != null && (room.getStatus() == 1 || room.getStatus() == 2)) {
            room.setStatus(bedCount > 0 && occupied >= bedCount ? 2 : 1);
        }
        roomMapper.updateById(room);
    }

    @Override
    public void updateStatus(Long roomId, Integer status) {
        Room room = getById(roomId);
        room.setStatus(status);
        roomMapper.updateById(room);
    }

    @Override
    public void restoreStatusFromOccupancy(Long roomId) {
        Room room = getById(roomId);
        // 仅维修中(3)的房间按床位占用恢复状态，停用(0)/预留(4)等其他状态不覆盖
        if (room.getStatus() == null || room.getStatus() != 3) return;
        long occupied = bedMapper.selectCount(Wrappers.<Bed>lambdaQuery()
                .eq(Bed::getRoomId, roomId)
                .eq(Bed::getStatus, 2));
        room.setOccupiedBeds((int) occupied);
        int bedCount = room.getBedCount() == null ? 0 : room.getBedCount();
        room.setStatus(bedCount > 0 && occupied >= bedCount ? 2 : 1);
        roomMapper.updateById(room);
    }

    private void validateFloor(RoomSaveDTO dto) {
        Floor floor = floorMapper.selectById(dto.getFloorId());
        if (floor == null) throw new BizException("所属楼层不存在");
        if (!floor.getBuildingId().equals(dto.getBuildingId())) throw new BizException("所选楼层不属于该楼栋");
    }

    private void normalizeUtilityConfig(RoomSaveDTO dto) {
        Integer mode = dto.getSettlementMode();
        if (mode == null) {
            dto.setUtilityAccountCode(null);
            dto.setElectricityRule(0);
            dto.setWaterRule(0);
            return;
        }
        if (mode != 1 && mode != 2) throw new BizException("无效的结算方式");
        if (mode == 2 && !StringUtils.hasText(dto.getUtilityAccountCode())) {
            dto.setUtilityAccountCode(dto.getRoomNumber());
        }
        if (!StringUtils.hasText(dto.getUtilityAccountCode())) throw new BizException("水电账户编号不能为空");
        dto.setUtilityAccountCode(dto.getUtilityAccountCode().trim());
        if (dto.getElectricityRule() == null) dto.setElectricityRule(0);
        if (dto.getWaterRule() == null) dto.setWaterRule(0);
    }
    private String normalizeFacilities(String facilities) {
        if (!StringUtils.hasText(facilities)) return facilities;
        try {
            Map<String, Object> raw = objectMapper.readValue(facilities, new TypeReference<>() {});
            Map<String, Integer> normalized = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : raw.entrySet()) {
                String name = normalizeFacilityName(entry.getKey());
                Integer count = normalizeFacilityCount(entry.getValue());
                if (!StringUtils.hasText(name) || count == null || count <= 0) continue;
                normalized.merge(name, count, Integer::sum);
            }
            if (normalized.isEmpty()) return null;
            dictService.ensureItems("ROOM_FACILITY", normalized.keySet());
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new BizException("房间设施格式不正确");
        }
    }

    private String normalizeFacilityName(String name) {
        if (!StringUtils.hasText(name)) return null;
        String trimmed = name.trim();
        return LEGACY_FACILITY_NAMES.getOrDefault(trimmed, trimmed);
    }

    private Integer normalizeFacilityCount(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

}
