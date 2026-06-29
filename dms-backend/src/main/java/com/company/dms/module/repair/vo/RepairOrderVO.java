package com.company.dms.module.repair.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RepairOrderVO {
    private Long id;
    private String orderNo;
    private Long roomId;
    private String roomNumber;
    private String buildingName;
    private Long residentId;
    private String residentName;
    private String title;
    private String description;
    private Integer priority;
    private Integer status;
    private String handler;
    private LocalDateTime acceptedAt;
    private String result;
    private LocalDateTime completedAt;
    private String remark;
}
