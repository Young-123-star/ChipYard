package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resource.dto.BoardQuery;
import com.company.dms.module.resource.dto.RoomQuery;
import com.company.dms.module.resource.dto.RoomSaveDTO;
import com.company.dms.module.resource.entity.Floor;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.FloorMapper;
import com.company.dms.module.resource.mapper.RoomMapper;
import com.company.dms.module.resource.vo.RoomBoardVO;
import com.company.dms.module.resource.vo.RoomSummaryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;
    private final FloorMapper floorMapper;
    private final com.company.dms.module.resource.mapper.BedMapper bedMapper;

    public RoomServiceImpl(RoomMapper roomMapper, FloorMapper floorMapper,
                           com.company.dms.module.resource.mapper.BedMapper bedMapper) {
        this.roomMapper = roomMapper;
        this.floorMapper = floorMapper;
        this.bedMapper = bedMapper;
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
    public Room getByRoomNumber(String roomNumber) {
        Room r = roomMapper.selectOne(Wrappers.<Room>lambdaQuery()
                .eq(Room::getRoomNumber, roomNumber).last("limit 1"));
        if (r == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "room not found");
        return r;
    }

    @Override
    public Long create(RoomSaveDTO dto) {
        Long count = roomMapper.selectCount(Wrappers.<Room>lambdaQuery()
                .eq(Room::getBuildingId, dto.getBuildingId())
                .eq(Room::getRoomNumber, dto.getRoomNumber()));
        if (count > 0) throw new BizException("该楼栋已存在相同房间号");
        Room r = new Room();
        BeanUtils.copyProperties(dto, r);
        r.setOccupiedBeds(0);
        roomMapper.insert(r);
        return r.getId();
    }

    @Override
    public void update(Long id, RoomSaveDTO dto) {
        getById(id);
        Room r = new Room();
        BeanUtils.copyProperties(dto, r);
        r.setId(id);
        roomMapper.updateById(r);
    }

    @Override
    public void delete(Long id) {
        getById(id);
        roomMapper.deleteById(id);
    }

    @Override
    public void refreshOccupancy(Long roomId) {
        Room room = getById(roomId);
        long occupied = bedMapper.selectCount(com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.company.dms.module.resource.entity.Bed>lambdaQuery()
                .eq(com.company.dms.module.resource.entity.Bed::getRoomId, roomId)
                .eq(com.company.dms.module.resource.entity.Bed::getStatus, 2));
        room.setOccupiedBeds((int) occupied);
        int bedCount = room.getBedCount() == null ? 0 : room.getBedCount();
        if (room.getStatus() != null && (room.getStatus() == 1 || room.getStatus() == 2)) {
            room.setStatus(occupied >= bedCount ? 2 : 1);
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
        long occupied = bedMapper.selectCount(com.baomidou.mybatisplus.core.toolkit.Wrappers.<com.company.dms.module.resource.entity.Bed>lambdaQuery()
                .eq(com.company.dms.module.resource.entity.Bed::getRoomId, roomId)
                .eq(com.company.dms.module.resource.entity.Bed::getStatus, 2));
        room.setOccupiedBeds((int) occupied);
        int bedCount = room.getBedCount() == null ? 0 : room.getBedCount();
        room.setStatus(occupied >= bedCount ? 2 : 1);
        roomMapper.updateById(room);
    }

}
