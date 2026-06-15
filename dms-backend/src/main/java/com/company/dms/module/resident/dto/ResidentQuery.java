package com.company.dms.module.resident.dto;

import lombok.Data;

@Data
public class ResidentQuery {
    private String realName;
    private String employeeNo;
    private Integer residentType;
    private Integer status;
    private long page = 1;
    private long size = 10;
}
