package com.company.dms.module.checkin.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.checkin.dto.*;
import com.company.dms.module.checkin.entity.CheckinIntake;
import com.company.dms.module.checkin.vo.IntakeVO;
import com.company.dms.module.checkin.vo.RecordVO;

public interface CheckinService {
    PageResult<IntakeVO> pageIntakes(IntakeQuery query);
    CheckinIntake getIntake(Long id);
    Long createManualIntake(IntakeCreateDTO dto);
    /** 供 integration 适配器调用；按 bizNo 幂等。 */
    Long createIntakeFromCommand(CreateIntakeCommand cmd);
    /** 选床确认入住：占床 + 刷新房间统计 + 生成档案 + 意向单置已入住（事务）。 */
    Long assign(Long intakeId, AssignDTO dto);
    void cancel(Long intakeId);
    PageResult<RecordVO> pageRecords(RecordQuery query);
}
