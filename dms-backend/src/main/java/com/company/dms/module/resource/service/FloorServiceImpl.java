package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resource.dto.FloorSaveDTO;
import com.company.dms.module.resource.entity.Floor;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.BuildingMapper;
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
    private final BuildingMapper buildingMapper;

    public FloorServiceImpl(FloorMapper floorMapper, RoomMapper roomMapper, BuildingMapper buildingMapper) {
        this.floorMapper = floorMapper;
        this.roomMapper = roomMapper;
        this.buildingMapper = buildingMapper;
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
        if (floor == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "楼层不存在");
        return floor;
    }

    @Override
    public Long create(FloorSaveDTO dto) {
        checkBuildingExists(dto.getBuildingId());
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
        checkBuildingExists(dto.getBuildingId());
        Long count = floorMapper.selectCount(Wrappers.<Floor>lambdaQuery()
                .eq(Floor::getBuildingId, dto.getBuildingId())
                .eq(Floor::getFloorNumber, dto.getFloorNumber())
                .ne(Floor::getId, id));
        if (count > 0) throw new BizException("该楼栋已存在相同楼层号");
        Floor f = new Floor();
        BeanUtils.copyProperties(dto, f);
        f.setId(id);
        floorMapper.updateById(f);
    }

    @Override
    public void delete(Long id) {
        if (floorMapper.selectById(id) == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "楼层不存在");
        Long roomCount = roomMapper.selectCount(Wrappers.<Room>lambdaQuery().eq(Room::getFloorId, id));
        if (roomCount > 0) throw new BizException("楼层下存在房间，不能删除");
        floorMapper.deleteById(id);
    }

    private void checkBuildingExists(Long buildingId) {
        if (buildingMapper.selectById(buildingId) == null) throw new BizException("所属楼栋不存在");
    }
}
