package com.company.dms.module.integration.hcp;

import com.company.dms.module.checkin.dto.CreateIntakeCommand;
import com.company.dms.module.checkin.service.CheckinService;
import com.company.dms.module.resident.dto.ResidentSaveDTO;
import com.company.dms.module.resident.service.ResidentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class HcpEmployeeAdapter {

    private final ResidentService residentService;
    private final CheckinService checkinService;
    private final ObjectMapper objectMapper;

    public HcpEmployeeAdapter(ResidentService residentService, CheckinService checkinService, ObjectMapper objectMapper) {
        this.residentService = residentService;
        this.checkinService = checkinService;
        this.objectMapper = objectMapper;
    }

    /** HCP 新员工 → upsert 居住人(来源2HCP) → 建意向单（bizNo=HCP-工号-入职日，幂等）。 */
    public Long handle(HcpEmployeeDTO dto) {
        ResidentSaveDTO r = new ResidentSaveDTO();
        r.setEmployeeNo(dto.getEmployeeNo());
        r.setRealName(dto.getName());
        r.setGender(dto.getSex());
        r.setResidentType(dto.getEmpType());
        r.setDeptName(dto.getDepartment());
        r.setPhone(dto.getMobile());
        r.setIdCard(dto.getIdCard());
        r.setStatus(1);
        Long residentId = residentService.upsertByEmployeeNo(r, 1);

        CreateIntakeCommand cmd = new CreateIntakeCommand();
        cmd.setBizNo("HCP-" + dto.getEmployeeNo() + "-" + (dto.getEntryDate() == null ? "NA" : dto.getEntryDate()));
        cmd.setSource(2);
        cmd.setResidentId(residentId);
        cmd.setExpectCheckinDate(parseDate(dto.getEntryDate()));
        cmd.setGenderLimitReq(dto.getSex());
        cmd.setRemark("HCP 新员工入职");
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
