package com.company.dms.module.checkin.service;

import com.company.dms.common.result.PageResult;
import com.company.dms.module.checkin.dto.*;
import com.company.dms.module.checkin.entity.CheckinIntake;
import com.company.dms.module.checkin.entity.CheckinRecord;
import com.company.dms.module.checkin.vo.IntakeVO;
import com.company.dms.module.checkin.vo.RecordVO;

import java.time.LocalDate;

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
    /** 取入住档案；不存在抛异常。 */
    CheckinRecord getRecord(Long id);
    /** 取该居住人当前在住(status=1)档案；无则返回 null。 */
    CheckinRecord findActiveRecordByResident(Long residentId);
    /** 办理退宿：置档案已退宿(2) + 回填退宿日。 */
    void markCheckedOut(Long recordId, LocalDate checkoutDate);
    /** 列出所有在住(status=1)档案。 */
    java.util.List<CheckinRecord> listActiveRecords();
    /** 按房间取在住(status=1)档案。 */
    java.util.List<CheckinRecord> listActiveRecordsByRoom(Long roomId);
    java.util.List<CheckinRecord> listRecordsByRoomAt(Long roomId, LocalDate date);
}
