package com.company.dms.module.resource.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.resource.dto.BuildingQuery;
import com.company.dms.module.resource.dto.BuildingSaveDTO;
import com.company.dms.module.resource.entity.Building;
import com.company.dms.module.resource.vo.BuildingVO;

public interface BuildingService {
    PageResult<BuildingVO> page(BuildingQuery query);
    Building getById(Long id);
    Long create(BuildingSaveDTO dto);
    void update(Long id, BuildingSaveDTO dto);
    void delete(Long id);
}
