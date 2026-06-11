package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resource.dto.RoomQuery;
import com.company.dms.module.resource.dto.RoomSaveDTO;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.mapper.RoomMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomMapper roomMapper;

    public RoomServiceImpl(RoomMapper roomMapper) {
        this.roomMapper = roomMapper;
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
    public Room getById(Long id) {
        Room r = roomMapper.selectById(id);
        if (r == null) throw new BizException(ResultCode.NOT_FOUND.getCode(), "房间不存在");
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
}
