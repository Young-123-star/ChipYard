package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resource.dto.FloorSaveDTO;
import com.company.dms.module.resource.entity.Floor;
import com.company.dms.module.resource.mapper.FloorMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FloorServiceImpl implements FloorService {

    private final FloorMapper floorMapper;

    public FloorServiceImpl(FloorMapper floorMapper) {
        this.floorMapper = floorMapper;
    }

    @Override
    public List<Floor> listByBuilding(Long buildingId) {
        return floorMapper.selectList(Wrappers.<Floor>lambdaQuery()
                .eq(Floor::getBuildingId, buildingId)
                .orderByAsc(Floor::getFloorNumber));
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
