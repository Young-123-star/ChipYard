package com.company.dms.module.resource.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.dms.common.exception.BizException;
import com.company.dms.common.result.PageResult;
import com.company.dms.common.result.ResultCode;
import com.company.dms.module.resource.dto.BuildingQuery;
import com.company.dms.module.resource.dto.BuildingSaveDTO;
import com.company.dms.module.resource.entity.Building;
import com.company.dms.module.resource.mapper.BuildingMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class BuildingServiceImpl implements BuildingService {

    private final BuildingMapper buildingMapper;

    public BuildingServiceImpl(BuildingMapper buildingMapper) {
        this.buildingMapper = buildingMapper;
    }

    @Override
    public PageResult<Building> page(BuildingQuery query) {
        Page<Building> p = buildingMapper.selectPage(
                Page.of(query.getPage(), query.getSize()),
                Wrappers.<Building>lambdaQuery()
                        .like(query.getBuildingName() != null, Building::getBuildingName, query.getBuildingName())
                        .eq(query.getStatus() != null, Building::getStatus, query.getStatus())
                        .orderByDesc(Building::getId));
        return PageResult.of(p);
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
