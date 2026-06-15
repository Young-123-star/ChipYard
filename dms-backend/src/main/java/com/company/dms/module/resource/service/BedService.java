package com.company.dms.module.resource.service;

import com.company.dms.module.resource.dto.BedSaveDTO;
import com.company.dms.module.resource.entity.Bed;
import java.util.List;

public interface BedService {
    List<Bed> listByRoom(Long roomId);
    Long create(BedSaveDTO dto);
    void update(Long id, BedSaveDTO dto);
    void delete(Long id);
    Bed getById(Long id);
    /** 占用床位：置已入住(2) + current_user_id=residentId。 */
    void occupy(Long bedId, Long residentId);
    /** 释放床位：置空闲(1) + current_user_id=null（退宿子项目用）。 */
    void release(Long bedId);
}
