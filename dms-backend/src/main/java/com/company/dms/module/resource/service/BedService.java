package com.company.dms.module.resource.service;

import com.company.dms.module.resource.dto.BedSaveDTO;
import com.company.dms.module.resource.entity.Bed;
import java.util.List;

public interface BedService {
    List<Bed> listByRoom(Long roomId);
    Long create(BedSaveDTO dto);
    void update(Long id, BedSaveDTO dto);
    void delete(Long id);
}
