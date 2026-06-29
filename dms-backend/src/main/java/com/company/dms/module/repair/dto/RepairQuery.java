package com.company.dms.module.repair.dto;

import lombok.Data;

@Data
public class RepairQuery {
    private Integer status;
    private Integer priority;
    private Long roomId;
    private long page = 1;
    private long size = 10;
}
