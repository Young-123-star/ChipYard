package com.company.dms.module.repair.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.repair.dto.RepairAcceptDTO;
import com.company.dms.module.repair.dto.RepairCompleteDTO;
import com.company.dms.module.repair.dto.RepairCreateDTO;
import com.company.dms.module.repair.dto.RepairQuery;
import com.company.dms.module.repair.entity.RepairOrder;
import com.company.dms.module.repair.vo.RepairOrderVO;

public interface RepairService {
    PageResult<RepairOrderVO> pageOrders(RepairQuery query);
    RepairOrder getOrder(Long id);
    RepairOrderVO getDetail(Long id);
    Long create(RepairCreateDTO dto);
    void accept(Long id, RepairAcceptDTO dto);
    void complete(Long id, RepairCompleteDTO dto);
    void cancel(Long id);
}
