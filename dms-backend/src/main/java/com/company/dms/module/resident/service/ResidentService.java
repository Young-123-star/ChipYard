package com.company.dms.module.resident.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.resident.dto.ResidentQuery;
import com.company.dms.module.resident.dto.ResidentSaveDTO;
import com.company.dms.module.resident.entity.Resident;

public interface ResidentService {
    PageResult<Resident> page(ResidentQuery query);
    Resident getById(Long id);
    Resident getByEmployeeNo(String employeeNo);
    Long create(ResidentSaveDTO dto);
    void update(Long id, ResidentSaveDTO dto);
    void delete(Long id);
    /** 按工号 upsert：存在则更新返回原 id，不存在则插入。source=居住人来源（1HCP 2手工 3OA）。 */
    Long upsertByEmployeeNo(ResidentSaveDTO dto, int source);
}
