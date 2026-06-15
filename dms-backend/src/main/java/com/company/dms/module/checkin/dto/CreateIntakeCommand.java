package com.company.dms.module.checkin.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateIntakeCommand {
    private String bizNo;
    private Integer source;
    private Long residentId;
    private LocalDate expectCheckinDate;
    private Integer genderLimitReq;
    private Integer roomTypeReq;
    private Long buildingIdReq;
    private String remark;
    private String rawPayload;
}
