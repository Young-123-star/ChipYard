package com.company.dms.module.checkin.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RecordVO {
    private Long id;
    private Long residentId;
    private String residentName;
    private String employeeNo;
    private Long buildingId;
    private Long floorId;
    private Long roomId;
    private Long bedId;
    private LocalDate checkinDate;
    private Integer status;
}
