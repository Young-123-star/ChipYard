package com.company.dms.module.checkin.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class IntakeVO {
    private Long id;
    private String bizNo;
    private Long residentId;
    private String residentName;
    private String employeeNo;
    private Integer residentGender;
    private Integer source;
    private LocalDate expectCheckinDate;
    private Integer genderLimitReq;
    private Integer roomTypeReq;
    private Long buildingIdReq;
    private String remark;
    private Integer status;
}
