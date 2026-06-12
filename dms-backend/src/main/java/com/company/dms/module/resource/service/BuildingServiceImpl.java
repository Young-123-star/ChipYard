package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resource.dto.BuildingQuery;
import com.company.dms.module.resource.dto.BuildingSaveDTO;
import com.company.dms.module.resource.entity.Building;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.BuildingMapper;
import com.company.dms.module.resource.mapper.RoomMapper;
import com.company.dms.module.resource.vo.BuildingVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BuildingServiceImpl implements BuildingService {

    private final BuildingMapper buildingMapper;
    private final RoomMapper roomMapper;

    public BuildingServiceImpl(BuildingMapper buildingMapper, RoomMapper roomMapper) {
        this.buildingMapper = buildingMapper;
        this.roomMapper = roomMapper;
    }

    @Override
    public PageResult<BuildingVO> page(BuildingQuery query) {
        Page<Building> p = buildingMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<Building>lambdaQuery()
                        .like(query.getBuildingName() != null, Building::getBuildingName, query.getBuildingName())
                        .eq(query.getStatus() != null, Building::getStatus, query.getStatus())
                        .orderByDesc(Building::getId));

        List<Long> ids = p.getRecords().stream().map(Building::getId).collect(Collectors.toList());
        Map<Long, List<Room>> roomsByBuilding = ids.isEmpty() ? Map.of()
                : roomMapper.selectList(Wrappers.<Room>lambdaQuery().in(Room::getBuildingId, ids))
                        .stream().collect(Collectors.groupingBy(Room::getBuildingId));

        Page<BuildingVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(p.getRecords().stream().map(b -> {
            BuildingVO vo = new BuildingVO();
            BeanUtils.copyProperties(b, vo);
            List<Room> rooms = roomsByBuilding.getOrDefault(b.getId(), List.of());
            vo.setRealRoomCount(rooms.size());
            vo.setRealBedCount(rooms.stream().mapToInt(r -> r.getBedCount() == null ? 0 : r.getBedCount()).sum());
            vo.setOccupiedBeds(rooms.stream().mapToInt(r -> r.getOccupiedBeds() == null ? 0 : r.getOccupiedBeds()).sum());
            return vo;
        }).collect(Collectors.toList()));
        return PageResult.of(voPage);
    }

    @Override
    public Building getById(Long id) {
        Building b = buildingMapper.selectById(id);
        if (b == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "楼栋不存在");
        return b;
    }

    @Override
    public Long create(BuildingSaveDTO dto) {
        Long count = buildingMapper.selectCount(
                Wrappers.<Building>lambdaQuery().eq(Building::getBuildingCode, dto.getBuildingCode()));
        if (count > 0) throw new BizException("楼栋编码已存在");
        Building b = new Building();
        BeanUtils.copyProperties(dto, b);
        buildingMapper.insert(b);
        return b.getId();
    }

    @Override
    public void update(Long id, BuildingSaveDTO dto) {
        getById(id);
        Building b = new Building();
        BeanUtils.copyProperties(dto, b);
        b.setId(id);
        buildingMapper.updateById(b);
    }

    @Override
    public void delete(Long id) {
        getById(id);
        buildingMapper.deleteById(id);
    }
}
