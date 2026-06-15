package com.company.dms.module.integration.oa;

import com.company.dms.module.checkin.dto.CreateIntakeCommand;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.resident.dto.ResidentSaveDTO;
import com.company.dms.module.resident.service.ResidentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class OaApplicationAdapter {

    private final ResidentService residentService;
    private final CheckinService checkinService;
    private final ObjectMapper objectMapper;

    public OaApplicationAdapter(ResidentService residentService, CheckinService checkinService, ObjectMapper objectMapper) {
        this.residentService = residentService;
        this.checkinService = checkinService;
        this.objectMapper = objectMapper;
    }

    /** 外部报文 → 解析/创建居住人 → 内部命令 → 创建意向单（幂等）。返回意向单 id。 */
    public Long handle(OaCheckinApplicationDTO dto) {
        ResidentSaveDTO r = new ResidentSaveDTO();
        r.setEmployeeNo(dto.getEmployeeNo());
        r.setRealName(dto.getEmployeeName());
        r.setGender(dto.getGender());
        r.setStatus(1);
        Long residentId = residentService.upsertByEmployeeNo(r, 3);

        CreateIntakeCommand cmd = new CreateIntakeCommand();
        cmd.setBizNo(dto.getApplicationNo());
        cmd.setSource(1);
        cmd.setResidentId(residentId);
        cmd.setExpectCheckinDate(parseDate(dto.getExpectDate()));
        cmd.setGenderLimitReq(dto.getGenderLimit());
        cmd.setRoomTypeReq(dto.getRoomType());
        cmd.setBuildingIdReq(dto.getBuildingId());
        cmd.setRemark(dto.getNote());
        cmd.setRawPayload(toJson(dto));
        return checkinService.createIntakeFromCommand(cmd);
    }

    private LocalDate parseDate(String s) {
        try { return s == null ? null : LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); } catch (Exception e) { return null; }
    }
}
