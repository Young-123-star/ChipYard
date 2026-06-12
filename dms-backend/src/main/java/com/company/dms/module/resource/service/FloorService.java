package com.company.dms.module.resource.service;

import com.company.dms.module.resource.dto.FloorSaveDTO;
import com.company.dms.module.resource.vo.FloorVO;
import java.util.List;

public interface FloorService {
    List<FloorVO> listByBuilding(Long buildingId);
    Long create(FloorSaveDTO dto);
    void update(Long id, FloorSaveDTO dto);
    void delete(Long id);
}
