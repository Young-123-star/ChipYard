package com.company.dms.module.checkin.dto;

import lombok.Data;

@Data
public class IntakeQuery {
    private Integer status;
    private Integer source;
    private long page = 1;
    private long size = 10;
}
