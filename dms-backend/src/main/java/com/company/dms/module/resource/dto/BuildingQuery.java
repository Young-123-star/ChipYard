package com.company.dms.module.resource.dto;

import lombok.Data;

@Data
public class BuildingQuery {
    private String buildingName;
    private Integer status;
    private long page = 1;
    private long size = 10;
}
