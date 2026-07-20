package com.company.dms.module.checkin.dto;

import lombok.Data;

@Data
public class RecordQuery {
    private Long buildingId;
    private Long roomId;
    private Integer status;
    private long page = 1;
    private long size = 10;
}
