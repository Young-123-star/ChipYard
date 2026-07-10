package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resource.dto.FloorSaveDTO;
import com.company.dms.module.resource.entity.Floor;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.FloorMapper;
import com.company.dms.module.resource.mapper.RoomMapper;
import com.company.dms.module.resource.vo.FloorVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FloorServiceImpl implements FloorService {

    private final FloorMapper floorMapper;
    private final RoomMapper roomMapper;

    public FloorServiceImpl(FloorMapper floorMapper, RoomMapper roomMapper) {
        this.floorMapper = floorMapper;
        this.roomMapper = roomMapper;
    }

    @Override
    public List<FloorVO> listByBuilding(Long buildingId) {
        List<Floor> floors = floorMapper.selectList(Wrappers.<Floor>lambdaQuery()
                .eq(Floor::getBuildingId, buildingId)
                .orderByAsc(Floor::getFloorNumber));
        Map<Long, List<Room>> roomsByFloor = roomMapper.selectList(Wrappers.<Room>lambdaQuery()
                        .eq(Room::getBuildingId, buildingId))
                .stream()
                .collect(Collectors.groupingBy(Room::getFloorId));

        return floors.stream().map(f -> {
            FloorVO vo = new FloorVO();
            BeanUtils.copyProperties(f, vo);
            List<Room> rooms = roomsByFloor.getOrDefault(f.getId(), List.of());
            vo.setRoomCount(rooms.size());
            vo.setBedCount(rooms.stream().mapToInt(r -> r.getBedCount() == null ? 0 : r.getBedCount()).sum());
            vo.setOccupiedBeds(rooms.stream().mapToInt(r -> r.getOccupiedBeds() == null ? 0 : r.getOccupiedBeds()).sum());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Floor getById(Long id) {
        Floor floor = floorMapper.selectById(id);
        if (floor == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "?????");
        return floor;
    }

    @Override
    public Long create(FloorSaveDTO dto) {
        Long count = floorMapper.selectCount(Wrappers.<Floor>lambdaQuery()
                .eq(Floor::getBuildingId, dto.getBuildingId())
                .eq(Floor::getFloorNumber, dto.getFloorNumber()));
        if (count > 0) throw new BizException("该楼栋已存在相同楼层号");
        Floor f = new Floor();
        BeanUtils.copyProperties(dto, f);
        floorMapper.insert(f);
        return f.getId();
    }

    @Override
    public void update(Long id, FloorSaveDTO dto) {
        if (floorMapper.selectById(id) == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "楼层不存在");
        Floor f = new Floor();
        BeanUtils.copyProperties(dto, f);
        f.setId(id);
        floorMapper.updateById(f);
    }

    @Override
    public void delete(Long id) {
        if (floorMapper.selectById(id) == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "楼层不存在");
        floorMapper.deleteById(id);
    }
}
