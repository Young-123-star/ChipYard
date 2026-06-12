package com.company.dms.module.resource.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.resource.dto.BoardQuery;
import com.company.dms.module.resource.dto.RoomQuery;
import com.company.dms.module.resource.dto.RoomSaveDTO;
import com.company.dms.module.resource.entity.Room;
import com.company.dms.module.resource.vo.RoomBoardVO;
import com.company.dms.module.resource.vo.RoomSummaryVO;
import java.util.List;

public interface RoomService {
    PageResult<Room> page(RoomQuery query);
    RoomSummaryVO summary(RoomQuery query);
    Room getById(Long id);
    Long create(RoomSaveDTO dto);
    void update(Long id, RoomSaveDTO dto);
    void delete(Long id);
    List<RoomBoardVO> board(BoardQuery query);
}
