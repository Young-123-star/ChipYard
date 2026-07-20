package com.company.dms.module.fee.dto;

import lombok.Data;

@Data
public class BillQuery {
    private String period;
    private Integer status;
    private Integer billType;
    private Long residentId;
    private Long roomId;
    private long page = 1;
    private long size = 10;
}
