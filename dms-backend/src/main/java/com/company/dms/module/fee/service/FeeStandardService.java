package com.company.dms.module.fee.service;

import com.company.dms.module.fee.dto.FeeStandardDTO;
import com.company.dms.module.fee.entity.FeeStandard;

import java.util.List;

public interface FeeStandardService {
    List<FeeStandard> list();
    FeeStandard getById(Long id);
    /** 按房型查；无则返回 null。 */
    FeeStandard findByRoomType(Integer roomType);
    Long create(FeeStandardDTO dto);
    void update(Long id, FeeStandardDTO dto);
    void delete(Long id);
}
