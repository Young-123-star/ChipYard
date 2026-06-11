package com.company.dms.module.resource.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.resource.dto.RoomQuery;
import com.company.dms.module.resource.dto.RoomSaveDTO;
import com.company.dms.module.resource.entity.Room;

public interface RoomService {
    PageResult<Room> page(RoomQuery query);
    Room getById(Long id);
    Long create(RoomSaveDTO dto);
    void update(Long id, RoomSaveDTO dto);
    void delete(Long id);
}
