package com.company.dms.module.fee.dto;

import lombok.Data;

@Data
public class MeterQuery {
    private String period;
    private Long buildingId;
    private String accountCode;
    private Long roomId;
    private Integer meterType;
    private long page = 1;
    private long size = 10;
}
